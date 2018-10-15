# unifn

[![Clojars Project](https://img.shields.io/clojars/v/unifn.svg)](https://clojars.org/unifn)

A Clojure library designed to provide "universal"
functions interface, which is composable by "meta"-data
into pipelines and workflows. 

Inspired by pedestal, prismatic libraries & onyx.


## Motivation

Some pieces of code could be expressed 
as a pipeline (for example middleware in ring etc) or in more 
general - a workflow


```
->[fn]-event->[fn]-event->[fn]

```

Ring uses functions decoration to build pipeline, but it has some drawbacks
- i.e. async interfaces, introspection, stacktraces etc

Let say we have defined interface for function:

function passed only one parameter and it's hash-map - universal argument
function produce hash-map, which will be merged into original argument and
passed downstream.

```
{:param ...} => f -> {:update ...} => {:param ... :update ... :metadata ...}
```

This functions could be composed into pipeline:

```
input => f =event=> f =event=> output

```

Pipeline or in general workflow described by data. 
Each function has unique key and some configuration data, which could modify
function behavior:


```clj
(defmethod unifn/*fn
   :my/step1
   [{cfg :my.step1/config ....}]
   ...)

(defmethod unifn/*fn
   :my/step2
   [{cfg :my.step2/config ....}]
   ...)
   
(defaction ::my/pipe
  {::u/fn [{::u/fn :my/step1
            :my.step1/config {...}}
           {::u/fn :my/step2
            :my.step2/config {...}}]})
```


Each message/argument has some meta attributes

```clj
::u/id - configured action id
::u/type = equals to ::u/fn  - type of event
::u/ts - timestamp

arg could have spcial keys

::u/tracers [tracer-fn]

{ tracer-fn [ev arg] }

```

We use one function interface

function get one hash-map argument with all context 
and returns hash-map which will be merged into original argument and passed
downstream

``` clj
(defmethod unifn/ufn 
  :my/key
  [arg] {:patch "patch"})

(unifn/apply {:unifn/fn :my/key
              :unifn/id "some-id"
              :unifn/event :module.module.action
              :some-config {}} 
             {:a 1}) 

;;=> 
   {:a 1 :patch "patch" 
    :unifn/event module.module.action or fn/key by default
    :unifn/id "some-id"
    
    :unifn/pipe ? like in pedestal

    :my/key {:some-config {}}}

(unifn/apply [{:unifn/fn :uniq/fn} {:unifn/fn :other/fn}] {:a 1})

(unifn/apply [:uniq/fn :other/fn] {:a 1})

```

->[fn]-event->[fn]-event->[fn]


## TODO

* interface one or two attributes?
* tracing
* naming {:id :event :fn}
* pipeline interface
* subscriptions


## Usage


To define unifunction you have to
implement mulitmethod with your key `:my/transform`:

```

(defmethod unifn/*apply-fn :my/transform
  [f arg]
  (assoc arg :var "value"))
```


You could call your function by `(unifn/apply f arg)`


```
(unifn/apply {:unifn/fn :my/transform} {}) 
=>  {:var "value"}

```

You could build pipeline of functions using :unifn/fn :unifn/pipe


```
{:unifn/fn   :unifn/pipe
 :unifn/id   "mypipline"
 :unifn/pipe [{:unifn/id "some id"
               :unifn/fn :test/transform}
              {:unifn/fn :unifn/fork
               :action   :test/interceptor
               :branch   [{}...]}
              {:unifn/fn :test/response}]}
```


```clj
{::u/id :my.rest.api
 ::u/tracer  fn 
 ::u/fn [{::u/fn :http/in ::u/tags #{:http.request.start} ::u/to :http/request}
         {::u/fn ::u/assoc-in [:metadata] ::u/value #'metadata/get-metadata'}
         {::u/fn :env/env ::u/to [:env]}
         {::u/fn :pg/connection :pg/from [:env]}
         {::u/fn :http/router :routes ...}
         {::u/fn :http/debug} ;; stops pipe here and return info
         {::u/filter {:u/stats :error} ...}
         {::u/fn :http/format :formats ...}
         {::u/fn :http/trace-request} ;; return stack for frontend
         {::u/fn :http/out 
          ::u/tags #{:http.request.end} 
          ::u/from :http/request
          ::u/subs [...]}]}
```


To stop/intercept pipeline you function should return :unifn/status :error or :stop:


```clj
(defmethod unifn/ufn 
  :test/interceptor
  [f arg]
  (when ...
    {:response {:interecepted true} :unifn/status :stop}))
```

## License

Copyright © 2017 niquola

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
