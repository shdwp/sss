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

(defn update [gs -gs]
  (gs/update gs
             ggmap/skip-ruler
             (ggmap/actor-door-ruler (gs/gd-data-path :ship :map))
             (ggmap/actor-use-ruler (gs/gd-data-path :ship :map))
             ggmap/actor-ruler
             (ggmap/actor-collision-ruler (gs/gd-data gs :ship :map))))

(defn paint [canvas gs]
  (let [gmap (-> (gs/gd-data gs :ship :map)
                 (gmap/put (entity/bitmap (gs/actor gs)) 
                           (-> (gs/actor gs) :x) 
                           (-> (gs/actor gs) :y))
                 gmap/as-bitmap
                 (view/view-bitmap-centered 30 20 (:x (gs/actor gs)) (:y (gs/actor gs))))]
    (canvas/paint canvas gmap :t 0 :l 0)))

(defn align [gs]
  (gs/gd-align 
    gs
    {:name :ship
     :block-input true
     :paint paint
     :update update}
    {:map (ship/gen-map 
                 (get-in 
                   (:universe gs) 
                   (drop-last 2 (:actor-path gs))))}))
