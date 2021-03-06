(ns health-monitor.api
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

(def default-response {:headers {"Content-Type" "application/json"}})
(def default-error-response (assoc default-response :status 500))
(def errors {:cmd-dispatcher/timeout (assoc default-error-response :body (clj->js {:message "timeout occured"}))
             :cmd-dispatcher/no-handler (assoc default-error-response :body (clj->js {:message "no handler found"}))
             :cmd-dispatcher/error (assoc default-error-response :body (clj->js {:message "an unknown error occured"}))})

(defn wrap-exception [resp]
  (get errors (first resp) resp))

(defn make-routes [{:keys [accept] :as cmd-dispatcher}]
  (compojure/routes
   (compojure/GET "/hello/:name" req (http-server/with-channel req channel
                                       (accept {:cmd/name :cmds/hello
                                                :cmd/timeout 5000
                                                :cmd/?data {:name (get-in req [:params :name])}
                                                :cmd/?reply (fn [resp]
                                                              (http-server/send! channel (wrap-exception resp)))})))
   (compojure-route/not-found (assoc default-response :body (clj->js {:message "not found"})))))

(defrecord RestAPI [make-routes cmd-dispatcher]
  component/Lifecycle
  (start [component]
    (log/infof "Starting API")
    (assoc component :routes (make-routes cmd-dispatcher)))
  (stop [component]
    (log/infof "Stopping API")
    component))
