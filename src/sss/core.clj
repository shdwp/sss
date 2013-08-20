(ns sss.core
  (:require [sss.ship.gen]
            [sss.game.ship.core :refer :all]
            [sss.game.game :as game]
            [sss.game.core]
            [sss.ship.core]
            [sss.graphics.core :as graphics]
            [sss.tile.core :as tile])
  (:gen-class))

;; Dev stuff
;; Notice me about migrating to Stuart Sierra's (reload)

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
  `(do (game/shutdown)
       (.interrupt @*gt*)))

(defn -main [& args]
  (s!))
