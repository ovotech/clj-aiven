(defproject ovotech/clj-aiven "0.0.2"
  :description "Client lib for aiven api"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.8.0"]
                 [clj-http "3.7.0"]
                 [clojure-future-spec "1.9.0-alpha17"]
                 [org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:dependencies [[clj-http-fake "1.0.3"]]}})
