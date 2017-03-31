# unifn

A Clojure library designed to provide universal 
functions interface, which is composable by meta-data
into chains and workflows. Inspired by pedestal & onyx

## Usage

To definy unifunction you have to
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

You could build pipeline of functions using :unifn/fn :unifn/pipeline


```
{:unifn/fn :unifn/pipe
                  :unifn/pipe [{:unifn/fn :test/transform}
                               {:unifn/fn :test/interceptor}
                               {:unifn/fn :test/response}]}
```


To stop/intercept pipeline you function should return :unifn/status :error or :stop:


```
(defmethod unifn/*apply-fn :test/interceptor
  [f arg]
  (if ...
    (merge arg {:response {:interecepted true}
                :unifn/status :stop})
    arg))
```

## License

Copyright © 2017 niquola

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
