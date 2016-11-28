(ns health-monitor.cmd-test
  (:require [clojure.test :refer :all]
            [health-monitor.cmd :as cmd]
            [clojure.core.async :as a]))

(defmacro test-async [ms done-name & body]
  (let [done-ch (gensym) v (gensym) c (gensym)]
    `(let [~done-ch (a/chan)
           ~done-name (fn [] (a/close! ~done-ch))]
       ~@body
       (let [[~v ~c] (a/alts!! [(a/timeout ~ms) ~done-ch])]
         (if (not= ~c ~done-ch)
           (is (= "Test completed in time" false)))))))

(deftest test-process
  (testing "cmd from buffer is processed properly"
    (test-async 500 done
                (let [buffer (a/chan (a/buffer 1))]
                  (cmd/process buffer (fn [cmd]
                                        (is (= cmd true))
                                        (a/close! buffer)
                                        (done)))
                  (a/put! buffer true))))
  (testing "cmd's ?reply called with error in case handle function throws exception"
    (test-async 500 done
                (let [buffer (a/chan (a/buffer 1))]
                  (cmd/process buffer (fn [& _]
                                        (throw (Exception.))))
                  (a/put! buffer {:cmd/?reply (fn [resp]
                                                (is (= :cmd-dispatcher/error (first resp)))
                                                (done))})))))

(deftest test-accept
  (testing "accepted request arrives in buffer"
    (let [buffer (a/chan (a/buffer 1))
          accept (cmd/accept buffer)]
      (is (= (accept {:test 123}) true))
      (is (= 123 (:test (a/<!! buffer))))))
  (testing "invalid request is rejected"
    (is (= ((cmd/accept nil) {:cmd/timeout -5}) false)))
  (testing "accepted request responded to with timeout if not answered in time"
    (test-async 500 done
                (let [buffer (a/chan (a/buffer 1))
                      accept (cmd/accept buffer)]
                  (accept {:cmd/timeout 100
                           :cmd/?reply (fn [resp]
                                         (is (= resp [:cmd-dispatcher/timeout]))
                                         (done))}))))
  (testing "accepted request responded to with proper response"
    (test-async 500 done
                (let [buffer (a/chan (a/buffer 1))
                      accept (cmd/accept buffer)]
                  (accept {:cmd/?reply (fn [resp]
                                         (is (= resp true))
                                         (done))})
                  ((:cmd/?reply (a/<!! buffer)) true)))))
