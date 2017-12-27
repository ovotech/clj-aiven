# clj-aiven

A Clojure library designed to interact with aiven api.

## Usage
```clojure
;Get topic lag
(require 'clj-aiven.core)


(def aiven-conn {:project "proj-name"
                 :service "serv-name"
                 :token   "aivenv1 token"})
              
                 
(topic-info aiven-conn "topic-name")

(topic-consumer-lag conn "topic-name" "consumer-name")           


----------------------------------------------------------------------------------


;Get topic sizes

(def schema-conn {:url  "https://kafka-uat.ovo-uat.aivencloud.com:13584"
                  :user "user"
                  :pass "password"})
                  
(list-topic-sizes conn schema-conn)                  
```
