(ns core.async.http.client.node
  (:refer-clojure :exclude [get])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :refer [chan close! >! <!]]
            [core.async.http.client :as c]
            [core.async.http.protocols :as proto])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(def ^:private http-node (node/require "http"))
(def ^:private https-node (node/require "https"))
(def ^:private url-node (node/require "url"))
(def ^:private querystring-node (node/require "querystring"))


(def options {:hostname "localhost"
              :port     8083
              :pathname "endpoint-1"
              :method   "GET"})

(def client
  (reify proto/Client
    (request! [_ {:keys [url
                         method
                         body
                         status-chan
                         headers-chan
                         body-chan
                         error-chan
                         timeout
                         headers] :as options}]

      (let [parsed-url (.parse url-node url)
            node-options {:protocol (.-protocol parsed-url)
                          :hostname (.-hostname parsed-url)
                          :port     (.-port parsed-url)
                          :path     (.-pathname parsed-url)
                          :method   (c/convert-method-name method)
                          ;:query    (.-query parsed-url)
                          }
            chans {:status-chan  (or status-chan (chan 1))
                   :headers-chan (or headers-chan (chan 1))
                   :body-chan    (or body-chan (chan 1024))
                   :error-chan   (or error-chan (chan 1))}
            req (.request
                  http-node (clj->js node-options)
                  (fn [res]
                    (go
                      (>! (chans :status-chan) (.-statusCode res))
                      (>! (chans :headers-chan) (js->clj (.-headers res))))

                    (.on res "connect"
                         (fn [response]
                           (println "got response")))
                    (.on res "data"
                         (fn [chunk]
                           (println "got something " (.toString chunk "utf-8"))
                           (go (>! (chans :body-chan) (.toString chunk "utf-8")))))
                    (.on res "end"
                         (fn []
                           (println "end request")))
                    (.on res "close"
                         (fn []
                           (println "close request")))))]

        (.on req "error"
             (fn [err]
               (println "got some err " err)
               (do
                 (close! (chans :status-chan))
                 (close! (chans :headers-chan))
                 (close! (chans :body-chan))
                 (go (>! (chans :error-chan) :timeout)))))

        (println "Node options: " node-options)
        (.end req)
        {:status  (chans :status-chan)
         :headers (chans :headers-chan)
         :body    (chans :body-chan)
         :error   (chans :error-chan)}))

    (sync-request! [this options]
      nil)))


(def request (partial c/request client))

(def get (partial c/get client))

(def post (partial c/post client))

;
;(defn -main [& args]
;  (println "Hello world!"))
;
;(set! *main-cli-fn* -main)
