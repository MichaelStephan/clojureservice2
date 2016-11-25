(ns health-monitor.core
  (:require [com.stuartsierra.component :as component]
            [health-monitor.cmd :as cmd]
            [health-monitor.service :as service]
            [health-monitor.api :as api]
            [health-monitor.webserver :as webserver])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn prod-system []
  (component/system-map
   :cmd-dispatcher (cmd/map->CmdDispatcher {:handle service/handle
                                            :buffer-size 1024})
   :api (component/using
         (api/map->RestAPI {})
         [:cmd-dispatcher])
   :webserver (component/using
               (webserver/map->WebServer {:port 6667})
               [:api])))

(def system (prod-system))

(defn -main [& args]
  (alter-var-root #'system component/start))

(comment
  (alter-var-root #'system component/stop))
