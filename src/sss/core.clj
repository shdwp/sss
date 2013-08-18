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
