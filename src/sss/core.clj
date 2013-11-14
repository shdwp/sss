(ns sss.core
  (:require [sss.ship.gen]
            [sss.game.ship.core :refer :all]
            [sss.game.game :as game]
            [sss.game.core]
            [sss.ship.core]
            [sss.graphics.core :as graphics]
            [sss.tile.core :as tile])
  (:gen-class))

(def gs (atom nil))
(def ^:dynamic *commit-time* (atom false))

(defn ts [] (reset! *commit-time* true))
(defn te [] (reset! *commit-time* false))

(defn commit-ruler [_gs -gs]
  (if @*commit-time*
    @gs
    (reset! gs _gs)))

(defn mk-path [path]
  (reduce
    (fn [p v]
      (if (= :actor v)
        (concat p [:universe] (:actor-path @gs))
        (concat p (list v))))
    '()
    path))

(defn s [& pv]
  (let [path (mk-path (butlast pv))
        value (last pv)]
    (swap! gs #(update-in % path (fn [_] value)))
    nil))

(defn u [& pv]
  (let [path (mk-path (butlast pv))
        ifn (last pv)]
    (swap! gs #(update-in % path ifn))
    nil))

(defn gs-save [f]
  (sss.game.gamestate/save @gs f)
  nil)

(defn gs-load [f]
  (swap! gs sss.game.gamestate/load f)
  nil)

;; Dev stuff
;; Notice me about migrating to Stuart Sierra's (reload)

(def ^:dynamic *game-thread* (atom nil))

(defn s! []
  (->> #(sss.game.core/continue "state")
      (Thread.)
      (reset! *game-thread*)
      (.start)))

(defmacro ! []
  `(do (game/shutdown)
       (.interrupt @*game-thread*)))

(defn -main [& args]
  (s!))
