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
(defmulti pre-condition (fn [f arg] (:unifn/fn f)))

(defmethod *apply-fn :default
  [f arg]
  (merge arg {:unifn/status :error
              :unifn/message (str "Could not resolve " f)}))

(defn apply-fn [{f-name :unifn/fn :as f} arg]
  (when-let [tr (:unifn/tracer arg)] (tr (merge arg {:unifn/phase :before :unifn/event f-name})))
  (let [arg (merge arg (dissoc f :unifn/fn))]
    (if-let [problems (and (s/get-spec f-name) (s/explain-data f-name arg))]
      (let [ev {:unifn/status :error
                :unifn/event f-name
                :unifn/problems (:clojure.spec/problems problems)}]
        (when-let [tr (:unifn/tracer arg)] (tr ev))
        (merge arg ev))
      (let [patch (if (:unifn/safe? arg)
                    (try (*apply-fn f arg)
                         (catch Exception e
                           {:unifn/status :error
                            :unifn/stacktrace (with-out-str (stacktrace/print-stack-trace e))}))
                    (*apply-fn f arg))
            patch (cond (map? patch) patch (nil? patch) {} :else {:unifn/value patch})
            patch (assoc patch :unifn/event f-name)
            res (deep-merge arg patch)
            res (cond-> res (:unifn/trace? arg) (update :unifn/trace (fn [x] (if x (conj x f-name) [f-name]))))]
        (when-let [tr (:unifn/tracer arg)] (tr (assoc patch :unifn/phase :after)))
        res))))

(defmethod *apply-fn :unifn/pipe
  [f arg]
  (loop [[f & fs] (:unifn/pipe f)
         arg arg]
    (cond
      (nil? f) arg
      (contains? #{:error :stop} (:unifn/status arg)) arg
      :else (recur fs (apply-fn f arg)))))

(defmethod *apply-fn :unifn/identity
  [f arg] {})
