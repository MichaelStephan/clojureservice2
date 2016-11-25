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
(def defaut-timeout-response (assoc default-error-response :body (clj->js {:message "timeout occured"})))
(def default-no-handler-response (assoc default-error-response :body (clj->js {:message "no handler found"})))
(def default-unknown-error-response (assoc default-error-response :body (clj->js {:message "an unknown error occured"})))

(defn wrap-exception [resp]
  (condp = (first resp)
    :cmd-dispatcher/timeout defaut-timeout-response
    :cmd-dispatcher/no-handler default-no-handler-response
    :cmd-dispatcher/error default-unknown-error-response
    resp))

(defn routes [{:keys [accept] :as cmd-dispatcher}]
  (compojure/routes
   (compojure/GET "/hello/:name" req (http-server/with-channel req channel
                                       (accept {:cmd :cmds/hello
                                                :timeout 5000
                                                :?data {:name (get-in req [:params :name])}
                                                :?reply (fn [resp]
                                                          (http-server/send! channel (wrap-exception resp)))})))
   (compojure-route/not-found (assoc default-response :body (clj->js {:message "not found"})))))

(defrecord RestAPI [cmd-dispatcher]
  component/Lifecycle
  (start [component]
    (log/infof "Starting API")
    (assoc component :routes (routes cmd-dispatcher)))
  (stop [component]
    (log/infof "Stopping API")
    component))
