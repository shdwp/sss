(ns sss.game.ship.core
  "Game-dispatcher for game on ship"
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

;; @TODO
(defn game-turn [gs]
  (-> gs
      (gs/log-turn)
      (assoc :ship (gmap/run-dispatcher (:ship gs)))))

(defn game-cycle 
  "Main ship cycle"
  [-gs]
  (let [gs (assoc -gs :last-cycle (System/currentTimeMillis))
        gs (gs/update gs
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
                 gmap/as-bitmap
                 (view/view-bitmap-centered 30 20 (:x (gs/actor gs)) (:y (gs/actor gs))))
        canvas (-> (canvas/canvas 50 40)
                   (canvas/paint gmap :t 0 :l 0)
                   (canvas/paint (gr/string (str "tick_" (:tick gs))) :t 0 :l 32)
                   (canvas/paint (gr/string 
                                   (str "tps_" 
                                        (int (/ (apply + (:fps gs)) (count (:fps gs))))
                                        )) :t 1 :l 32)
                   (canvas/paint (gr/text-buffer 
                                   (map 
                                     #(str "(" (first %) ") " (second %)) 
                                     (:log gs)) 
                                   20 
                                   10) :t 2 :l 32)
                   (term/term-painter gs))
        gs (-> gs
               (update-in [:fps]
                          (fn [f]
                            (cons
                              (/ 1000 (- (:last-cycle gs) (:last-cycle -gs)))
                              (butlast f)))))
        ]
    (send (:view gs) (fn [_] canvas))
    (gs/limit-tps gs 20)
    (recur gs)))

(def ^:dynamic *gui-thread* (atom nil))

(defn start 
  "Start game on ship with ~gs"
  [gs]
  (->> #(lanclj/view! (:view gs) (:input gs))
       Thread.
       (reset! *gui-thread*)
       .start)
  (game-cycle (assoc gs :ship (ship/gen-map 
                                (get-in 
                                  (:universe gs) 
                                  (drop-last 2 (:actor-path gs)))))))

(defn shutdown 
  "Shutdown game (must be called for proper gui threads interruption)"
  []
  (lanclj/shutdown!)
  (.interrupt @*gui-thread*))
