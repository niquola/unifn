(ns unifn
  (:require [clojure.spec :as s]
            [clojure.stacktrace :as stacktrace]))

(defn deep-merge [a b]
  (loop [[[k v :as i] & ks] b
         acc a]
    (if (nil? i)
      acc
      (let [av (get a k)]
        (if (= v av)
          (recur ks acc)
          (recur ks (if (and (map? v) (map? av))
                      (assoc acc k (deep-merge av v))
                      (assoc acc k v))))))))

(defmulti *fn (fn [arg] (:unifn/fn arg)))

(defmethod *fn :unifn/identity
  [arg] {})

(defmethod *fn :default
  [arg]
  {:unifn/status :error
   :unifn/message (str "Could not resolve " (:unifn/fn arg))})

(declare *apply)

(defn *apply-impl [{st :unifn/status inter :unifn/intercept f-name :unifn/fn tracers :unifn/tracers :as arg}]
  (if (and (not (= inter :all))
           (contains? #{:error :stop} st))
    arg
    (let [arg (dissoc arg :unifn/intercept)]
      (do
       (when tracers
         (let [trace-ev {:unifn/fn f-name :unifn/phase :enter}]
           (doseq [t tracers] (*apply t {:event trace-ev :arg arg}))))

       (if-let [problems (and (s/get-spec f-name) (s/explain-data f-name arg))]
         (let [ev {:unifn/status :error
                   :unifn/fn f-name
                   :unifn/problems (:clojure.spec/problems problems)}]
           (when tracers
             (doseq [t tracers] (*apply t {:event ev :arg arg})))
           (merge arg ev))
         (let [patch (if (:unifn/safe? arg)
                       (try (*fn arg)
                            (catch Exception e
                              {:unifn/status :error
                               :unifn/stacktrace (with-out-str (stacktrace/print-stack-trace e))}))
                       (*fn arg))
               patch (cond (map? patch) patch (nil? patch) {} :else {:unifn/value patch})
               res (deep-merge arg patch)]
           (when tracers
             (let [trace-ev (merge arg {:unifn/phase :leave})]
               (doseq [t tracers] (*apply t {:event patch :arg res}))))
           res))))))

(defn *apply [f arg]
  ;; validate f
  (cond
    (keyword? f) (*apply-impl (assoc arg :unifn/fn f))
    (map? f)     (*apply-impl (deep-merge arg f))
    (vector? f) (loop [[f & fs] f, arg arg]
                  (cond
                    (nil? f) arg
                    :else (recur fs (*apply f arg))))
    (var? f) (*apply (var-get f) arg)
    :else (throw (Exception. (str "I don't know how to apply " (pr-str f))))))

