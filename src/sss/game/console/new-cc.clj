(ns sss.game.console.new-cc
  (:require [sss.game.gamestate :as gs]
            [sss.game.console.core :as console]

            [sss.graphics.canvas :as can]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.gui.group :as g]
            [sss.graphics.gui.component :as cp]

            [sss.universe.core :as uni]
            [sss.universe.space.core :as space]
            [sss.universe.system.core :as system]
            [sss.ship.autopilot :as autopi]
            ))

(defn update [gs -gs]
  (g/update [:ccc :menu] gs -gs))

(defn paint [canvas gs]
  (g/paint canvas gs [:ccc :menu] :t 1 :l 1))

(defn init [gs]
  (-> gs
      (g/setup
        [:ccc :menu] 
        (g/menu-group-hotkeys
          [:ccc :menu] 
          [\1 \2 \3]
          (cp/text (fn [gs]
                     (let [path (gs/gd-data gs :ccc :path)
                           ship (gs/get-universe gs path)]
                       (str 
                         "ship " (:name ship)
                         "\nposition"
                         "\n    " (uni/unipath (:universe gs) 
                                            path 
                                            (:x ship) 
                                            (:y ship))
                         "\nautopilot " (if (autopi/enabled? ship) "on" "off")
                         "\n    to " (uni/unipath (:universe gs)
                                              (autopi/destination ship)
                                              -1 -1) 
                         ))))
          (cp/scrollable-bitmap (- (gs/gd-data gs :console :canvas :w) 2) (- (gs/gd-data gs :console :canvas :h) 2)
                                (fn [gs]
                                  (gs/gd-data gs :ccc :space-map)))
          (cp/scrollable-bitmap (- (gs/gd-data gs :console :canvas :w) 2) (- (gs/gd-data gs :console :canvas :h) 2)
                                (fn [gs]
                                  (system/visualize
                                    (gs/get-universe gs (take 3 (:actor-path gs)))
                                    (:turn gs))))))
      (gs/gd-set-data 
        [:ccc :space-map]   
        (space/visualize
          (gs/get-universe gs [])
          (space/pointed-data (gs/get-universe gs [:space]))
          :paint-callback (fn [c fx fy x y s]
                            (if-not (>= (uni/compare-paths [:space y x] 
                                                       (:actor-path gs)) 3)
                              c
                              (can/paint c (bm/bitmap "@") :t fy :l (dec fx))))))))

(defn align
  [gs path]
  (-> gs
      (console/align path)
      (gs/gd-align
        {:name :ccc
         :update update
         :paint paint}
        {:path path
         :mode :info})
      (init)))
