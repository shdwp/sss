(ns sss.game.console.core
  "High-level general game-dispatcher for dealing with consoles. Aligns to lower game-d"
  (:require [sss.graphics.core :as gr]
            [sss.graphics.viewport :as view]
            [sss.graphics.canvas :as canvas]
            [sss.game.gamestate :as gs]
            [sss.universe.core :as uni]
            [taoensso.timbre :refer [spy]]
            [sss.game.gmap :as ggmap]
            ))

(defn quit-ruler 
  "Ruler for quitting from console"
  [gs -gs]
  (if (gs/get-key-in gs #{\q})
    (gs/gd-pop gs :console)
    gs))

(defn update [gs gs-]
  (gs/update gs
             ggmap/skip-ruler
             quit-ruler))

(defn paint [canvas gs]
  (let [path (gs/gd-data gs :console :path)
        ship (gs/get-universe gs path)]
    (canvas/in-paint 
      (canvas/crop canvas (gs/gd-data gs :console :canvas :w) (gs/gd-data gs :console :canvas :h) 0 0 0 0)
      ((gr/rect (gs/gd-data gs :console :canvas :w) (gs/gd-data gs :console :canvas :h) \#) :t 0 :l 0)
      ((gr/string (str "$Console@" 
                       (uni/unipath (:universe gs) 
                                    path
                                    (:x ship) 
                                    (:y ship)))) :t 0 :l 0)
      )))

(defn align
  [gs path]
  (gs/gd-align
    gs
    {:name :console 
     :block-input true
     :block-paint true
     :update update
     :paint paint}
    {:path path
     :canvas {:h 20 :w 30}
     }))

