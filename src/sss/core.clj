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

(defmacro run-pst [& forms]
  `(try
     ~@forms
     (catch Exception e# (.printStackTrace e#))))

(defmacro s []
  `(run-pst (sss.game.core/start)))

(defmacro ! []
  `(sss.game.ship.core/shutdown))

(defn -main [& args]
  (s))
