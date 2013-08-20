(ns sss.game.console.direct-control
  (:require [sss.game.gamestate :as gs]
            [sss.ship.core :as ship]
            [sss.graphics.gui.core :as gui]
            [sss.graphics.viewport :as view]
            [sss.graphics.canvas :as canvas]
            [sss.universe.system.core :as sys]
            ))

(defn ship-ruler 
  "Ruler for ship moving"
  [gs -gs]
  (or (if-not (gs/turn? gs -gs)
        (if-let [dir (gs/direction (gs/get-key-in gs [\h \j \k \l]))]
          (let [xy (gs/xy-apply-dir dir 0 0)
                ship (gs/get-universe gs (gs/gd-data gs :console :path))]
            (gs/turn 
              (gs/update-universe gs 
                                  (gs/gd-data gs :console :path) 
                                  ship/move (first xy) (second xy))))))
      gs))

(defn update [gs -gs]
  (gs/update gs
             ship-ruler
             ))

(defn paint [canvas gs]
  (canvas/paint
    canvas
    (view/view-bitmap
      (sys/visualize (gs/get-universe gs (take 3 (gs/gd-data gs :console :path))) 
                     (:turn gs))
      48 28 :t 0 :l 0)
    :t 1 :l 1))

(defn align [gs path]
  (gs/gd-align 
    gs
    {:name :dc-console
     :update update
     :paint paint}
    {:map nil}))
     
