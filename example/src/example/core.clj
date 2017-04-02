(ns example.core
  (:require [org.httpkit.server :as http-kit]
            [ring.util.codec]
            [cheshire.core :as json]
            [cheshire.generate :as json-gen]
            [unifn :as u])
  (:import org.httpkit.server.AsyncChannel))

(json-gen/add-encoder org.httpkit.server.AsyncChannel json-gen/encode-str)
(json-gen/add-encoder clojure.lang.Var json-gen/encode-str)

(defmethod u/*fn
  :my/users
  [{{uri :http/uri params :http/query-params} :http/request
    users :my/users}]
  {:http/response {:http/body (or users [])
                   :http/status 200}})

(defmethod u/*fn
  :my/routing
  [{routes :my/routes
   {uri :http/uri} :http/request
   :as arg}]
  (if-let [route (get routes uri)]
    (u/*apply route arg)
    {:http/response {:http/body {:message (str uri " not found")} 
                     :http/status 404}}))

(defmethod u/*fn
  :tracer/endpoint
  [arg]
  {:http/response {:http/body @trace} :http/status 200}) 

(defmethod u/*fn
  :http/json-response
  [{{body :http/body} :http/response :as arg}]
  (when (and body (not (string? body)))
    {:http/response {:http/body (json/generate-string body)
                     :http/headers {"content-type" "application/json"}}}))

(defmethod u/*fn
  :ring/adapter
  [{{qs :query-string :as req} :request :as arg}]
  {:http/request {:http/uri (:uri req)
                  :http/body (:body req)
                  :http/query-params  (if qs (ring.util.codec/form-decode qs) {})}})

(defonce server (atom nil))

(defonce trace (atom []))

(reset! trace [])

(json/generate-string
 (last @trace))

(defmethod u/*fn
  :my/tracer [{ev :event arg :arg}]
  (println ev)
  (swap! trace conj (merge (dissoc ev :unifn/tracer) {:ts (java.util.Date.)})))

(defmethod u/*fn
  :env/inject [{to :env/into from :env/from}]
  (assoc {} to (var-get from)))

(def routes
  {"/" {::u/fn :my/users :my/users [{:name "niquola"}]}
   "/test" {::u/fn :my/users :my/users [{:name "niquola"}]}
   "/trace" :tracer/endpoint})

(def stack
  [:ring/adapter
   {::u/fn :env/inject :env/into :my/routes :env/from #'routes}
   {::u/fn :my/routing}
   :http/json-response])

(defn app [req]
  (let [{resp :http/response :as result}
        (u/*apply stack {:request req
                         ::u/tracers [:my/tracer]})]
    (if resp
      {:body    (:http/body resp)
       :status  (:http/status resp)
       :headers (:http/headers resp)}
      {:body (json/generate-string result)
       :status 500})))


(defn restart []
  (when-let [s @server] (@server))
  (reset! server (http-kit/run-server #'app {:port 5558})))

(comment
  (restart)

  )
