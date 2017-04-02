(defproject example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths  ["src"
                  "test"
                  "../src"
                  "../test"]
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [http-kit "2.2.0"]
                 [ring "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [matcho "0.1.0-RC3"]
                 [cheshire "5.7.0"]
                 [route-map "0.0.4"]])
