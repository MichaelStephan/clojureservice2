(defproject health-monitor-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-server" "-Xms512m" "-Xmx512m"]
  :main health-monitor.core
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojure-future-spec "1.9.0-alpha14"]
                 [com.taoensso/timbre "4.7.4"]
                 [http-kit "2.2.0"]
                 [org.clojure/core.async "0.2.395"]
                 [slingshot "0.12.2"]
                 [compojure "1.5.1"]
                 [environ "1.1.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.clojure/data.json "0.2.6"]
                 [ring-basic-authentication "1.0.5"]]
  :uberjar-name "app.jar"
  :profiles {:uberjar {:aot :all}})
