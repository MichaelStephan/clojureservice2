(ns health-monitor-clj.core
  (:require [com.stuartsierra.component :as component]
            [health-monitor-clj.service :as service]
            [health-monitor-clj.api :as api]
            [health-monitor-clj.webserver :as webserver])
  (:gen-class))

(defn prod-system []
  (component/system-map
   :cmd-dispatcher (service/map->CmdDispatcher {:buffer-size 1})
   :api (component/using
         (api/map->API {})
         [:cmd-dispatcher])
   :webserver (component/using
               (webserver/map->WebServer {:port 6667})
               [:api])))

(def system (prod-system))
(alter-var-root #'system component/start)
(alter-var-root #'system component/stop)
