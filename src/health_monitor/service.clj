(ns health-monitor.service
  (:require [taoensso.timbre :as log]))

(defmulti handle :cmd/name)

(defmethod handle :cmds/hello [{:keys [:cmd/?reply :cmd/?data]}]
  (?reply (str "hello " ?data)))

(defmethod handle :default [{:keys [:cmd/?reply :cmd/name]}]
  (log/warnf "No handler for %s found" name)
  (?reply [:cmd-dispatcher/no-handler]))
