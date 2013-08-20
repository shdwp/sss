(ns sss.game.game
  (:require [sss.gui.lanterna-clojure.core :as lanclj]

            [sss.graphics.core :as gr]
            [sss.graphics.viewport :as view]
            [sss.graphics.bitmap :as bitmap]
            [sss.graphics.canvas :as canvas]
            [sss.graphics.gui.term :as term]
            [sss.ship.core :as ship]
            [sss.gmap.core :as gmap]
            [sss.entity.actor :as actor]
            [sss.entity.core :as entity]
            [sss.tile.core :as tile]
            [sss.game.gamestate :as gs]
            [sss.game.gmap :as ggmap]
            [taoensso.timbre :refer [spy]]
            ))

(defn game-cycle 
  [gs-]
  (let [gs (gs/update gs-
                      gs/tps-ruler
                      term/term-ruler
                      (gs/gds-update)
                      (gs/tps-counter-ruler (:last-cycle gs-))
                      gs/turn-update)

        canvas (-> (canvas/canvas 50 40)
                   (gs/gds-paint gs)
                   (term/term-painter gs)
                   (canvas/paint (gr/string (str "turn_" (:turn gs))) :t 0 :r 50)
                   (canvas/paint (gr/string (str "tps_" (gs/average-tps gs))) :t 1 :r 50))]
    (send (:view gs) (fn [_] canvas))
    (gs/limit-tps! gs 20)
    (recur (update-in gs [:tick] inc))))

(def ^:dynamic *gui-thread* (atom nil))

(defn start 
  "Start game on ship with ~gs"
  [gs]
  (->> #(lanclj/view! (:view gs) (-> gs :input :buffer))
       Thread.
       (reset! *gui-thread*)
       .start)
  (game-cycle gs))

(defn shutdown 
  "Shutdown game (must be called for proper gui threads interruption)"
  []
  (lanclj/shutdown!)
  (.interrupt @*gui-thread*))
