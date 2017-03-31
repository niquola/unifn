(ns unifn.core-test
  (:require [unifn.core :as sut]
            [matcho.core :as matcho]
            [clojure.test :refer :all]
            [clojure.spec :as s]))

(defmethod sut/*apply-fn :test/transform
  [f arg]
  (assoc arg :var "value"))

(defmethod sut/*apply-fn :test/transform
  [f arg]
  (assoc arg :var "value"))

(defmethod sut/*apply-fn :test/response
  [f arg]
  (assoc arg :response {:some "response"}))

(defmethod sut/*apply-fn :test/interceptor
  [f arg]
  (if (:intercept arg)
    (merge arg {:response {:interecepted true}
                :unifn/status :stop})
    arg))

(defmethod sut/*apply-fn :test/throwing
  [f arg]
  (throw (Exception. "ups")))

(deftest unifn-basic-test
  (matcho/match
   (sut/apply-fn {:unifn/fn :test/transform} {:some "payload"})
   {:unifn/event :test/transform
    :var "value"
    :some "payload"})

  (matcho/match
   (sut/apply-fn {:unifn/fn :test/unexisting} {:some "payload"})
   {:unifn/event :test/unexisting
    :unifn/status :error})

  (is (thrown? Exception (sut/apply-fn {:unifn/fn :test/throwing} {:some "payload"})))

  (matcho/match
   (sut/apply-fn {:unifn/fn :test/throwing} {:some "payload" :unifn/safe? true})
   {:unifn/status :error
    :unifn/stacktrace string?})


  (matcho/match 
   (sut/apply-fn {:unifn/fn :unifn/pipe
                  :unifn/pipe [{:unifn/fn :test/transform}
                               {:unifn/fn :test/interceptor}
                               {:unifn/fn :test/response}]}
                 {:request {} :unifn/trace? true})

   {:response {:some "response"} :var "value" :unifn/event :unifn/pipe})

  (matcho/match 
   (sut/apply-fn {:unifn/fn :unifn/pipe
                  :unifn/pipe [{:unifn/fn :test/transform}
                               {:unifn/fn :test/interceptor}
                               {:unifn/fn :test/response}]}
                 {:request {} :intercept true})
   {:response {:interecepted true}}))
