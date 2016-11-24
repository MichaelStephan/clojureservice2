(ns health-monitor-clj.api
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as http-server]
            [taoensso.timbre :as log]
            [compojure.core :as compojure]
            [compojure.route :as compojure-route]
            [clojure.data.json :as json]))

(defn clj->js [data]
  (json/write-str (if (nil? data)
                    {}
                    data)))

(def default-error-response {:status 500
                             :headers {"Content-Type" "application/json"}})

(defn wrap-exception [resp]
  (condp = resp
    :cmd-dispatcher/timeout (merge default-error-response
                                   :body (clj->js {:message "timeout occured"}))
    :cmd-dispatcher/no-handler (merge default-error-response
                                      :body (clj->js {:message "no handler found"}))
    resp))

(defn routes [{:keys [accept] :as cmd-dispatcher}]
  (compojure/routes
   (compojure/GET "/hello/:name" req (http-server/with-channel req channel
                                       (accept {:cmd :cmds/hello
                                                :timeout 5000
                                                :?data {:name (get-in req [:params :name])}
                                                :?reply (fn [resp]
                                                          (http-server/send! channel (wrap-exception resp)))})))
   (compojure-route/not-found {:status 404
                               :headers {"Content-Type" "application/json"}
                               :body (clj->js {:message "not found"})})))

(defrecord API [cmd-dispatcher]
  component/Lifecycle
  (start [component]
    (log/infof "Starting API")
    (assoc component :routes (routes cmd-dispatcher)))
  (stop [component]
    (log/infof "Stopping API")
    component))
