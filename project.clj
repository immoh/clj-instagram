(defproject clj-instagram "0.3.0-SNAPSHOT"
  :description "Clojure client for Instagram API"
  :url "https://github.com/immoh/clj-instagram"
  :source-paths ["src"]
  :dependencies [[clj-http "3.3.0"]
                 [cheshire "5.6.3"]
                 [buddy/buddy-core "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/test.check "0.9.0"]
                                  [midje "1.8.3"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-codox "0.10.0"]]}}
  :codox {:project {:name "clj-instagram"}
          :source-uri "https://github.com/immoh/clj-instagram/blob/{version}/{filepath}#L{line}"})
