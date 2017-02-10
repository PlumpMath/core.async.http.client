(ns core.async.http.sample-endpoints
  (:require
    [immutant.web :as web]
    [immutant.web.async :as iasync]
    [clojure.string :as str]))

(def ^:private localhost "localhost")

(defn- streaming-endpoint [name num]
  (fn [request]
    (iasync/as-channel request
                       {:on-open (fn [stream]
                                   (do
                                     (iasync/send! stream name)
                                     (dotimes [msg num]
                                       (iasync/send! stream (str " part-" msg)
                                                     {:close? (= msg (- num 1))})
                                       (Thread/sleep 70))))})))

(defn- run-streaming-endpoint [name num]
  (web/run (streaming-endpoint name num) :host localhost :port 8083 :path (str "/" name)))

(defn- endpoint [name]
  (fn [request]
    {:status  200
     :headers {"X-Header-1"                  ["Value 1" "Value 2"]
               "Access-Control-Allow-Origin" "*"}
     :body    (str "Hello world and " name)}))

(defn- error-endpoint [request]
  {:status 500
   :body   "Hello world 500"})

(defn- error-sleep-endpoint [request]
  (Thread/sleep 1000)
  (error-endpoint request))

(defn- sleep-endpoint [request]
  (Thread/sleep 1000)
  {:status 200
   :body   (str "Hello world and sleep\n")})

(defn- headers-fragment [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (->> (seq (:headers request))
                 (filter (fn [[key value]] (str/starts-with? key "x-")))
                 (map (fn [[key value]] (str key ": " value)))
                 (str/join ", "))})

(defn- body-echo-fragment [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (request :body)})

(defn- run-endpoint [name]
  (web/run (endpoint name) :host localhost :port 8083 :path (str "/" name)))

(defn start []
  (run-endpoint "endpoint-1")
  (run-streaming-endpoint "streaming-endpoint-1" 3)
  (web/run error-endpoint :host localhost :port 8083 :path (str "/error"))
  (web/run error-sleep-endpoint :host localhost :port 8083 :path (str "/error-sleep"))
  (web/run sleep-endpoint :host localhost :port 8083 :path (str "/sleep"))
  (web/run headers-fragment :host localhost :port 8083 :path (str "/x-headers"))
  (web/run body-echo-fragment :host localhost :port 8083 :path (str "/echo")))

(defn -main []
  (println "Starting endpoints...")
  (start))