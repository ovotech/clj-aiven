(ns clj-aiven.lag-metrics-poller
  (:require [again.core :as again]
            [clj-aiven.core :as aiven]
            [clojure.tools.logging :as log]
            [iapetos.core :as prometheus]
            [overtone.at-at :refer [every mk-pool]]))

(def ^:private poller-pool (mk-pool))

(defn with-exp-backoff-fn
  "Retries (f) up to `max-retries`, waiting `initial-delay-ms` for the first retry
  and then with exponential larger waits (multiplied by `delay-multiplier`).
  NOTE: f *must* throw to trigger a retry"
  ([f] (with-exp-backoff-fn nil f))
  ([{:keys [initial-delay-ms
            delay-multiplier
            max-retries]
     :or   {initial-delay-ms 1000
            delay-multiplier 2.5
            max-retries      3}}
    f]
   (let [exp-backoff (->> (again/multiplicative-strategy initial-delay-ms delay-multiplier)
                          (again/max-retries max-retries)
                          vec)]
     (again/with-retries exp-backoff (f)))))

(defmacro with-exp-backoff
  "A cute little macro to please Dr. Dutton. So that he doesn't have to type `#`"
  [config & body]
  `(with-exp-backoff-fn ~config (fn [] ~@body)))

(defn record-lag-metrics
  [{:keys [metrics-registry aiven-conf retry-config application-id metric-key topic]
    :as   _opts}]
  (try
    (let [lag (->> application-id
                   (aiven/topic-consumer-lag aiven-conf topic)
                   (with-exp-backoff retry-config)
                   :total-lag)]
      (prometheus/set metrics-registry metric-key lag))
    (catch Exception e
      (log/error e "Failed to pull lag data."))))

(defn start-polling [{:keys [every-period-ms]
                      :or   {every-period-ms 300000}
                      :as   opts}]
  (every every-period-ms
         #(record-lag-metrics opts)
         poller-pool))

