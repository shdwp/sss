(ns sss.game.ship.core
  (:require [sss.gui.lanterna-clojure.core :as lanclj]
            [sss.gui.prn.core :as prngui]

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

(defn game-turn [gs]
  (-> gs
      (gs/log-turn)
      (assoc :ship (gmap/run-dispatcher (:ship gs)))))

(defn game-cycle [-gs]
  (let [gs (gs/update -gs
                      term/term-ruler
                      ggmap/skip-ruler
                      ggmap/actor-door-ruler
                      ggmap/actor-use-ruler
                      ggmap/actor-ruler
                      ggmap/actor-collision-ruler

                      gs/tick-update)

        gmap (-> (:ship gs) 
                 (gmap/put (entity/bitmap (gs/actor gs)) 
                           (-> (gs/actor gs) :x) 
                           (-> (gs/actor gs) :y))
                 gmap/as-canvas
                 (view/view-bitmap-centered 30 20 (:x (gs/actor gs)) (:y (gs/actor gs))))
        canvas (-> (canvas/canvas 50 40)
                   (canvas/paint gmap :t 0 :l 0)
                   (canvas/paint (gr/string (str "tick_" (:tick gs))) :t 0 :l 32)
                   (canvas/paint (gr/text-buffer 
                                   (map 
                                     #(str "(" (first %) ") " (second %)) 
                                     (:log gs)) 
                                   20 
                                   10) :t 1 :l 32)
                   (term/term-painter gs))]
    (send (:view gs) 
          (fn [_] 
            canvas))
    (Thread/sleep 100)
    (game-cycle gs)))

(def ^:dynamic *gui-thread* (atom nil))

(defn start [gs]
  (->> #(lanclj/view! (:view gs) (:input gs))
       Thread.
       (#(do (.setPriority % Thread/MAX_PRIORITY)
             %))
       (reset! *gui-thread*)
       .start)
  (game-cycle (assoc gs :ship (ship/gen-map 
                                (get-in 
                                  (:universe gs) 
                                  (drop-last 2 (:actor-path gs)))))))

(defn shutdown []
  (lanclj/shutdown!)
  (.interrupt @*gui-thread*))
