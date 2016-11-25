(ns health-monitor.service
  (:require [taoensso.timbre :as log]))

(defmulti handle :cmd)

(defmethod handle :cmds/hello [{:keys [?reply ?data]}]
  (when ?reply
    (?reply (str "hello " ?data))))

(defmethod handle :default [{:keys [?reply cmd]}]
  (log/warnf "No handler for %s found" cmd)
  (when ?reply
    (?reply [:cmd-dispatcher/no-handler])))
