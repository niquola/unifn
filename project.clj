(defproject unifn "0.1.0-SNAPSHOT"
  :description "A Clojure library designed to provide universal functions interface, which is composable by meta-data into chains and workflows. "
  :url "https://github.com/niquola/unifn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories   [["clojars"  {:url "https://clojars.org/repo" :sign-releases false}]]
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [ch.qos.logback/logback-classic "1.2.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [http-kit "2.2.0"]
                 [ring/ring-defaults "0.3.0"]
                 [cheshire "5.7.1"]
                 [clj-yaml "0.4.0"]
                 [clj-time "0.13.0"]
                 [route-map "0.0.4"]
                 [matcho "0.1.0-RC6"]])
