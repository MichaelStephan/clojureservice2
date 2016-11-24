(ns health-monitor-clj.webserver
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as http-server]
            [taoensso.timbre :as log]))

(defrecord WebServer [api port thread queue-size]
  component/Lifecycle
  (start [component]
    (log/infof "Starting WebServer on port %s" port)
    (assoc component :server (http-server/run-server (:routes api) {:port port
                                                                    :thread (or thread 4)
                                                                    :queue-size (or queue-size 1024)})))
  (stop [{:keys [server] :as component}]
    (log/infof "Stopping WebServer")
    (when server
      (server :timeout 100))
    (assoc component :server nil)))
