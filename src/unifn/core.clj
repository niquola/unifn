(ns unifn.core
  (:require [clojure.spec :as s]
            [clojure.stacktrace :as stacktrace]))

(defn- deep-merge [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

(s/def :unifn/fn-def (s/keys :req [:unifn/id :unifn/fn]))

(s/def :unifn/fn keyword?)

(defmulti *apply-fn (fn [f arg] (:unifn/fn f)))

(defmethod *apply-fn :default
  [f arg]
  (merge arg {:unifn/status :error
              :unifn/message (str "Could not resolve " (:unifn/fn f))}))

(defn apply-fn [f arg]
  (cond->
      (if (:unifn/safe? arg)
        (try
          (assoc (*apply-fn f arg) :unifn/event (:unifn/fn f))
          (catch Exception e
            (merge arg
                   {:unifn/event (:unifn/fn f)
                    :unifn/status :error
                    :unifn/stacktrace (with-out-str (stacktrace/print-stack-trace e))})))
        (assoc (*apply-fn f arg) :unifn/event (:unifn/fn f)))
    (:unifn/trace? arg) (update :unifn/trace (fn [x] (if x (conj x (:unifn/fn f)) [(:unifn/fn f)])))))

(defmethod *apply-fn :unifn/pipe
  [f arg]
  (loop [[f & fs] (:unifn/pipe f)
         arg arg]
    (cond
      (nil? f) arg
      (contains? #{:error :stop} (:unifn/status arg)) arg
      :else (recur fs (apply-fn f arg)))))
