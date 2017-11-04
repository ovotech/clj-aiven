(ns clj-aiven.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(def aiven-base "https://api.aiven.io/v1beta")

(defn get-http [{:keys [token]} url]
  (:body (http/get url
           {:headers {:Authorization token}
            :as      :auto})))

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