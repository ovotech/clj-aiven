# clj-aiven

A Clojure library designed to interact with aiven api. At preset simply gets kafka topic information and can also calculate lag.

## Usage
```clojure
(require 'clj-aiven.core)


(def aiven-conn {:project "proj-name"
                 :service "serv-name"
                 :token   "aivenv1 token"})
              
                 
(topic-info aiven-conn "topic-name")

(topic-consumer-lag conn "topic-name" "consumer-name")           


```
