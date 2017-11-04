(ns clj-aiven.core-test
  (:require [clojure.test :refer :all]
            [clj-aiven.core :refer :all]
            [cheshire.core :as json])
  (:use clj-http.fake))

(def conn {:project "project"
           :service "service"
           :token   "aivenv1 token"})

(defn assert-auth [request]
  (let [headers (:headers request)]
    (is (= {"Authorization" "aivenv1 token", "accept-encoding" "gzip, deflate"}
          headers))))

(deftest topics
  (testing "get topic info"
    (let [topic "mytopic"]
      (with-fake-routes {(str "https://api.aiven.io/v1beta/project/project/service/service/topic/" topic)
                         {:get (fn [request]
                                 (assert-auth request)
                                 {:status 200 :headers {} :body "{:topic \"stuff\"}"})}}
        (is (= "{:topic \"stuff\"}" (topic-info conn topic)))))))