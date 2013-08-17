(ns sss.core
  (:require [sss.ship.gen]
            [sss.game.ship.core :refer :all]
            [sss.game.core]
            [sss.ship.core]
            [sss.graphics.core :as graphics]
            [sss.tile.core :as tile])
  (:gen-class))

(defmacro dev []
  `(do 
     (in-ns 'user)
     (load "autoload")
     (autoload/autoload-thread! "src/sss" true)
     (in-ns 'sss.core)
     nil))

(def gs (atom nil))

(defn mk-path [path]
  (vec (flatten (map #(if (symbol? %) (keyword %) %) path))))

(defmacro g [& path] 
  (let [path# (mk-path path)]
    `(get-in @gs ~path#)))
(defmacro u [& pv] 
  (let [path# (mk-path (butlast pv))
        ifn# (last pv)]
    `(do (reset! gs (update-in @gs ~path# ~ifn#))
         nil)))
(defmacro s [& pv] 
  (let [path# (mk-path (butlast pv))
        value# (last pv)]
    `(do (reset! gs (update-in @gs ~path# (fn [x#] ~value#)))
         nil)))

(def actor
  [:universe :space 0 1 :ships 0 :entities 0])

(defmacro run-pst [& forms]
  `(try
     ~@forms
     (catch InterruptedException e# nil)
     (catch Exception e# (.printStackTrace e#))))

(def ^:dynamic *gt* (atom nil))

(defmacro s! []
  `(do
     (.start (reset! *gt* (Thread. #(sss.core/run-pst (sss.game.core/start)))))))

(defmacro ! []
  `(do (sss.game.ship.core/shutdown)
       (.interrupt @*gt*)))

(defn -main [& args]
  (s!))
