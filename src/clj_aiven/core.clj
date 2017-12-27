(ns clj-aiven.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]))

(s/def ::topic string?)
(s/def ::project string?)
(s/def ::service string?)
(s/def ::token string?)
(s/def ::connection (s/keys :req-un [::project ::service ::token]))

(s/def ::partition integer?)
(s/def ::lag integer?)
(s/def ::partition-lag (s/keys :req-un [::partition ::lag]))

(def ^:private aiven-base "https://api.aiven.io/v1beta")

(defn- get-http
  [{:keys [token]} url]
  (:body (http/get url
                   {:headers          {:Authorization (str "aivenv1 " token)}
                    :as               :json
                    :throw-exceptions false})))

(defn topic-info
  "Pulls back details of the specified topic."
  [{:keys [project service] :as conn} topic]
  (let [topic-endpoint
        (format "%s/project/%s/service/%s/topic/%s"
                aiven-base
                project
                service
                topic)]
    (get-http conn topic-endpoint)))

#_(s/fdef topic-info
          :args {:conn  ::connection
                 :topic ::topic})

(defn- parse-topic-partition-offset
  [{:keys [consumer_groups] :as partition} group-name]
  (let [consumer-group (filter #(= group-name (:group_name %)) consumer_groups)]
    (:offset (first consumer-group))))

(defn topic-consumer-lag
  "Pulls back a vector of partition/lag pairs for the specified topic and consumer."
  [conn topic group-name]
  (let [topic-data (topic-info conn topic)
        partitions (->> (get-in topic-data [:topic :partitions])
                        (map (fn [partition]
                               (when-let [offset (parse-topic-partition-offset partition group-name)]
                                 {:partition (:partition partition)
                                  :lag       (- (:latest_offset partition) offset)})))
                        (filter some?))]
    {:partitions partitions
     :total-lag  (reduce + 0 (map :lag partitions))}))


(defn get-subjects
  [{:keys [url user pass] :as conn}]
  (let [url  (format "%s/subjects" url)
        resp (http/get url
                       {:basic-auth [user pass]}
                       :as :json)]
    (-> resp
        :body
        (json/parse-string true))))


(defn topic-size
  [conn topic]
  (let [topic-data (topic-info conn topic)
        partitions (->> (get-in topic-data [:topic :partitions])
                        (map (fn [partition]
                               {:partition (:partition partition)
                                :size      (:size partition)}))
                        (filter some?))]
    {:topic           topic
     :retention_hours (get-in topic-data [:topic :retention_hours])
     :total-size-KBs  (reduce + 0 (map :size partitions))}))

(defn list-topic-sizes [conn schema-conn]
  (doall (let [subjects (get-subjects schema-conn)
               topics   (map #(clojure.string/replace % #"-value" "") subjects)]
           (reverse (sort-by :total-size-KBs (map #(topic-size conn %) topics))))))


#_(s/fdef topic-consumer-lag
          :args {:conn       ::connection
                 :topic      ::topic
                 :group-name string?}
          :ret (s/coll-of (s/keys :req-un [::total-lag ::partitions])))