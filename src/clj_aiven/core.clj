(ns clj-aiven.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json])
  (:use com.rpl.specter))

(def aiven-base "https://api.aiven.io/v1beta")

(defn get-http [{:keys [token]} url]
  (:body (http/get url
           {:headers {:Authorization token}
            :as      :json})))

(defn topic-info [{:keys [project
                          service]
                   :as   conn} topic]
  (let [topic-endpoint
        (format "%s/project/%s/service/%s/topic/%s"
          aiven-base
          project
          service
          topic)]
    (get-http conn topic-endpoint)))

(defn topic-consumer-lag [conn topic consumer-id]
  (let [topics (topic-info conn topic)]
    (mapv #(let [p-offset (:latest_offset %)
                 offset   (first (filter (complement nil?)
                                   (map (fn [x] (when (= (:group_name x) consumer-id) (:offset x))) (:consumer_groups %))))]
             (when offset (assoc (select-keys % [:partition]) :lag (- p-offset offset))))
      (get-in topics [:topic :partitions]))))