(ns unifn.scratch
  (:require  [clojure.test :as t]))








;; (def ensure-user  {:fn (fn [{req :request :as arg}] (assoc-in arg [:request :user] {:name "user"}))})
;; (def some-context {:fn (fn [{req :request :as arg}] (assoc-in arg [:request :context] {:data "data"}))})
;; (def exceptional   {:fn (fn [_] (throw (Exception. "ups")))})


;; (defn *interceptor [{request :request :as arg}]
;;   (if (:stop request)
;;     (merge arg {:unifn/status :stop
;;                 :response {:status 404}})
;;     arg))

;; (def interceptor {:fn *interceptor})

;; (defn *transformer [{request :request :as arg}]
;;   (update-in arg  [:request :counter] inc))

;; (def transformer {:fn *transformer})

;; (deftest test-unifn


;;   (sut/apply-fn {:fn identity} {:a 1 :unifn/debug true})

;;   (sut/apply-fn {:fn count} {:a 1})

;;   (is (thrown? Exception
;;                (sut/apply-fn exceptional {:a 1 :unifn/debug true})))
  
;;   (matcho/match
;;    (sut/apply-fn exceptional {:a 1 :unifn/debug true :unifn/safe true})
;;    {:unifn/status :error
;;     :unifn/exception #(not (nil? %))})


;;   (sut/apply-fn ensure-user {:request {:uri "ups"} :unifn/debug true})

;;   (sut/apply-chain [ensure-user some-context]  {:request {:uri "ups"} :unifn/debug true})


;;   (sut/apply-fn {:id :some-fn} {:a 1 :unifn/context {:some-fn identity}})

;;   (matcho/match
;;    (sut/apply-chain [interceptor transformer] {:request {:counter 1} :unifn/debug true})
;;    {:request {:counter 2}})

;;   (matcho/match
;;    (sut/apply-chain [interceptor transformer] {:request {:counter 1 :stop true}})
;;    {:response {:status 404}})

;;   )



(def action {:unifn/id :some-id 
             :unifn/fn :quilified/name})

;; { fhir$resourceType: "Patient"
;;   us$race: {
;;             type: "Codding"
;;             fhir$codding: ....
;;            }
;;  fhir$name: [{fhir$given ["some"]}]}

;; (def some-rest-endpoint
;;   {:unifn/id :just-rest
;;    :unifn/type :http/ring
;;    :unifn/fn  :rest/endpoint
;;    :rest/path [:get "/databases"]
;;    :rest/pipe [{:unifn/id :parse.params
;;                 :unifn/fn :http/query-string
;;                 :unifn/into [:request :params]}

;;                {:unifn/id :validate-params
;;                 :unifn/fn :json-schema/validate
;;                 :json-schema/value [:request :params]
;;                 :json-schema/schema {:name {:$pattern "^[a-z]*$"}}}

;;                {:unifn/id        :access-control
;;                 :unifn/fn        :policy/check-policy
;;                 :policy/policies [{:request {:user {:metadata {:role "admin"}}}}]}

;;                {:unifn/id     :select-tables
;;                 :unifn/fn     :honeysql/query
;;                 :unifn/into   [:response :body :tables]
;;                 :honeysql/sql {:select [:*]
;;                                :from [:tables]
;;                                :where [:ilike :name [:$ref [:request :params :name]]]}}

;;                {:unifn/id     :select-databases
;;                 :unifn/fn     :honeysql/query
;;                 :unifn/into   [:response :body :databases]
;;                 :honeysql/sql {:select [:*]
;;                                :from [:databases]}}]})
;; (def subscription
;;   {:unifn/subscribe-to [:create-external-patient]
;;    :unifn/actions [{:unifn/id     :notify-in-telegram
;;                     :unifn/fn     :telegram/message
;;                     :unifn/value  [:response :body]
;;                     :telegram/config {:$ref [:config :notify :admin :telegram]}}]})

;; (def other-rest-endpoint
;;   {:unifn/id :create-external-patient
;;    :unifn/fn  :swagger/endpoint
;;    :swagger/path [:post "Patient" "$external"]
;;    :swagger/action [{:unifn/id :parse.body
;;                      :unifn/fn :fhir/parse
;;                      :unifn/value {:$ref [:request :body]}
;;                      :unifn/into  [:request :resource]}

;;                     {:unifn/id :validate-resource
;;                      :unifn/fn :json-schema/validate
;;                      :json-schema/value {:$ref [:request :resource]}
;;                      :json-schema/schema-from [:metadata :json-schema :ExternalPractitioner]}

;;                     {:unifn/id     :create-external-patient
;;                      :unifn/fn     :fhir/create-resource
;;                      :fhir/resourceType "Patient"
;;                      :unifn/value [:request :resource]}

;;                     {:unifn/id     :notify-in-telegram
;;                      :unifn/fn     :telegram/message
;;                      :unifn/value  [:response :body]
;;                      :telegram/config {:$ref [:config :notify :admin :telegram]}}]})

;; ;; unifn/validate
;; ;; unifn/metadata :fn/name
;; ;; unifn/execute  :fn/name uniarg


;; (apply some-rest-endpoint
;;        {:request {:uri "/something" :request-method :get :query-string "name=views"}})
