(ns health-monitor.cmd
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as a]
            [taoensso.timbre :as log])
  (:use [slingshot.slingshot :only [try+ throw+]]))

(defn accept [buffer-ch]
  (fn [{:keys [?reply timeout] :or {timeout 10000} :as cmd}]
    (let [resp-ch (a/chan)
          reply (fn [resp]
                  (a/go
                    (when resp
                      (a/>! resp-ch resp))
                    (a/close! resp-ch)))]
      (a/go
        (a/>! buffer-ch (assoc cmd :?reply reply))
        (let [[v c] (a/alts! [(a/timeout timeout) resp-ch])]
          (?reply
           (if (= c resp-ch)
             v
             [:cmd-dispatcher/timeout])))))))

(defrecord CmdDispatcher [handle buffer-size]
  component/Lifecycle
  (start [component]
    (log/infof "Starting Cmd Dispatcher with buffer-size %s" buffer-size)
    (let [buffer (a/chan (a/buffer (if (> buffer-size 0)
                                     buffer-size
                                     512)))]
      (a/go-loop []
        (when-let [{:keys [?reply] :as cmd} (a/<! buffer)]
          (log/infof "Received cmd %s" cmd)
          (try+
           (handle cmd)
           (catch Object e
             (log/error "An unforseen exception occured" e)
             (?reply [:cmd-dispatcher/error e])))
          (recur)))
      (-> component
          (assoc :buffer buffer)
          (assoc :accept (accept buffer)))))
  (stop [{:keys [buffer] :as component}]
    (log/infof "Stopping Cmd Dispatcher")
    (a/close! buffer)
    (dissoc component :buffer)))
