(ns unifn.core
  (:require [clojure.spec :as s]
            [clojure.stacktrace :as stacktrace]))

(s/def :unifn/fn-def (s/keys :req [:unifn/id :unifn/fn]))

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

;; TODO: refactor to pedestal interceptors

;; Universal function interface to chain operation functions

;; (defn handler
;;   [{request :request :as arg}]
;;   (merge arg {:response arg
;;               :unifn/status :ok}))

;; (defn transformer
;;   [{request :request :as arg}]
;;   (if true
;;     (assoc-in arg [:request :some-field] "transform")
;;     (merge arg {:unifn/status :error
;;                 :response {:status 500}})))

;; (defn interceptor
;;   [{request :request :as arg}]
;;   (if true
;;     (merge arg {:responce {:status 404}
;;                 :unifn/status :stop})
;;     arg))


;; (defn- deep-merge [& maps]
;;   (if (every? map? maps)
;;     (apply merge-with deep-merge maps)
;;     (last maps)))

;; (defn resolve-fn [f-def ctx]
;;   (when (and f-def ctx)
;;     (let [f (get ctx (:id f-def))] f)))

;; (defn *apply-fn [f-def arg]
;;   (if-let [f (or (:fn f-def) (when-let [fm (resolve-fn f-def (:unifn/context arg))] (:fn fm)))]
;;     (let [res (f (deep-merge arg (or (:args f-def) {})))
;;           res (if (map? res) res {:value res})]
;;       (if (:unifn/debug arg)
;;         (update-in res [:unifn/log] (fn [x]
;;                                       (let [entry (merge f-def {:out res :in (dissoc arg :unifn/log)})]
;;                                         (if x (conj x entry) [entry]))))
;;         res))
;;     {:unifn/status :error
;;      :unifn/message (str "Coud not resolve fn: " f-def)}))

;; (defn apply-fn [f-def arg]
;;   (if (:unifn/safe arg)
;;     (try
;;       (*apply-fn f-def arg)
;;       (catch Exception e
;;         {:unifn/status :error
;;          :unifn/exception e}))
;;     (*apply-fn f-def arg)))


;; (defn apply-chain [fs arg]
;;   (loop [[f & fs] fs
;;          arg arg]
;;     (cond
;;       (nil? f) arg
;;       (contains? #{:error :stop} (:unifn/status arg)) arg
;;       :else (recur fs (apply-fn f arg)))))
