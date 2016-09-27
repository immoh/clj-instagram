(defproject clj-instagram "0.2.0-SNAPSHOT"
  :description "Clojure client for Instagram API"
  :url "https://github.com/immoh/clj-instagram"
  :source-paths ["src"]
  :dependencies [[clj-http "2.2.0"]
                 [cheshire "5.6.3"]
                 [buddy/buddy-core "0.13.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/test.check "0.9.0"]
                                  [midje "1.8.3"]]
                   :plugins [[lein-midje "3.2"]
                             [lein-codox "0.9.5"]]}}
  :codox {:project {:name "clj-instagram"}
          :source-uri "https://github.com/immoh/clj-instagram/blob/{version}/{filepath}#L{line}"})
