(defproject puzzle-crypt "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lock-key "1.5.0"]
                 [org.clojure/tools.cli "0.4.0"]]
  :main ^:skip-aot puzzle-crypt.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
