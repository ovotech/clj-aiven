(ns clj-aiven.core-test
  (:require [clojure.test :refer :all]
            [clj-aiven.core :refer :all]
            [cheshire.core :as json]
            [clj-http.util :as util]
            [clojure.spec.test.alpha :as stest])
  (:use clj-http.fake))

(stest/instrument)

(def conn {:project "project"
           :service "service"
           :token   "token"})

(defn assert-auth [request]
  (let [headers (:headers request)]
    (is (= {"Authorization" "aivenv1 token" "accept-encoding" "gzip, deflate"}
           headers))))

(def topic-response
  {:topic {:partitions [{:consumer_groups [{:group_name "wobble" :offset 12266138}
                                           {:group_name "bobble" :offset 19244049}]
                         :latest_offset   19244049
                         :partition       0}
                        {:consumer_groups [{:group_name "wobble" :offset 19244040}
                                           {:group_name "bobble" :offset 19244049}]
                         :latest_offset   19244049
                         :partition       1}]
           :state      "ACTIVE"
           :topic_name "mytopic"}})

(deftest get-topic-info
  (let [topic "mytopic"]
    (with-fake-routes {(str "https://api.aiven.io/v1beta/project/project/service/service/topic/" topic)
                       {:get (fn [request]
                               (assert-auth request)
                               {:status 200 :headers {} :body "{ \"key\": \"value\" }"})}}
                      (is (= {:key "value"} (topic-info conn topic))))))

(deftest get-topic-lag
  (let [topic "mytopic"]
    (with-fake-routes {(str "https://api.aiven.io/v1beta/project/project/service/service/topic/" topic)
                       {:get (fn [request]
                               (assert-auth request)
                               {:status 200 :headers {} :body (json/generate-string topic-response)})}}

                      (let [result (topic-consumer-lag conn topic ["wobble" "bobble"])]
                        (is (= {:partitions [{:partition 0 :lag 6977911}
                                             {:partition 1 :lag 9}]
                                :total-lag  6977920}
                               (get result "wobble")) "wobble partition lags match")
                        (is (= {:partitions [{:partition 0 :lag 0}
                                             {:partition 1 :lag 0}]
                                :total-lag  0}
                               (get result "bobble")) "bobble partition lags match")))))

