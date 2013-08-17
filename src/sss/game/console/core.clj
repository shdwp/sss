(ns sss.game.console.core
  (:require [sss.gui.lanterna-clojure.core :as lanclj]
            [sss.gui.prn.core :as prngui]
            [sss.graphics.core :as gr]
            [sss.graphics.viewport :as view]
            [sss.graphics.bitmap :as bitmap]
            [sss.graphics.canvas :as canvas]
            [sss.ship.core :as ship]
            [sss.gmap.core :as gmap]
            [sss.entity.actor :as actor]
            [sss.entity.core :as entity]
            [sss.tile.core :as tile]
            [sss.game.gamestate :as gs]
            [sss.game.gmap :as ggmap]
            [sss.universe.core :as uni]
            [taoensso.timbre :refer [spy]]
            ))

(defn game-turn [gs]
  gs)

(defn quit-ruler [gs -gs]
  (if (gs/get-key-in gs #{\q})
    (gs/set-quit gs)
    gs))

(defn game-cycle [-gs]
  (let [gs (gs/update -gs
                      quit-ruler
                      ggmap/skip-ruler
                      #((-> -gs :console :update) %1 %2)
                      gs/tick-update)
        ship (gs/get-universe gs (-> gs :console :path))
        bmap (canvas/in-paint (-> gs :console :screen)
                              ((gr/rect 50 20 \#) :t 0 :l 0)
                              ((gr/string (str "$Console@" 
                                               (uni/unipath (:universe gs) 
                                                            (-> gs :console :path) 
                                                            (:x ship) 
                                                            (:y ship)))) :t 0 :l 0))
        bmap ((-> gs :console :paint) bmap gs)]
    (send (:view gs) (fn [& _] (view/view-bitmap bmap -1 -1 :t 0 :l 0)))
    (Thread/sleep 33)
    (if (gs/quit? gs)
      (gs/quitted gs)
      (recur gs))))

(defn align [gs path t]
  (game-cycle (-> gs
                  (assoc :console {:update (:update t)
                                   :paint (:paint t)
                                   :path path
                                   :mode :info
                                   :screen (canvas/canvas 50 20)})
                  (gs/add-turner game-turn))))
