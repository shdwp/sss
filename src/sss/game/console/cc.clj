(ns sss.game.console.cc 
  (:require [sss.graphics.canvas :as canvas]
            [sss.graphics.core :as gr]
            [sss.graphics.viewport :as view]
            [sss.game.gamestate :as gs]
            [sss.universe.core :as uni]
            [sss.universe.system.core :as sys]
            [sss.universe.space.core :as space]
            [sss.ship.autopilot :as autopi]
            [sss.graphics.gui.core :as gui]
            [taoensso.timbre :refer [spy]]
            ))

(defn gate-menu [gs]
  (let [ship-path (take 5 (-> gs :console :path))
        system-path (take 3 ship-path)]
    (if (= (-> gs :console :mode) :gate)
      (gui/menu-sel gs 
                    (map 
                      second
                      (apply (partial space/get-near-systems 
                                      (gs/get-universe gs [:space]) 
                                      1)
                             (drop 1 system-path)))
                    #(-> %1
                         (gs/update-universe (concat system-path [:gate :queue]) 
                                             (fn [q] (conj q [ship-path %2])))
                         (update-in [:console :mode] (fn [_] :info))
                         (gs/tick)
                         (gs/set-quit)))
      gs)))

(defn update [gs -gs]
  (-> gs
      gate-menu
      (gui/mode-toggle [:console :mode] \m :info :map)
      (gui/mode-toggle [:console :mode] \M :info :gmap)
      (gui/mode-toggle [:console :mode] \g :info :gate)

      (gui/scrollable-in-modes [:console :mode] 
                               [:gmap :map] 
                               [:console :map :x] 
                               [:console :map :y])))

(defn paint [canvas gs]
  (let [path (-> gs :console :path)
        ship (gs/get-universe gs path)]
    (case (-> gs :console :mode)
      :info 
      (canvas/paint 
        canvas 
        (gui/text
          (str "ship " (:name ship))
          "position"
          (str " " (uni/unipath (:universe gs) 
                                path 
                                (:x ship) 
                                (:y ship)))
          (str "autopilot " (if (autopi/enabled? ship) "on" "off"))
          (str "to " (uni/unipath (:universe gs)
                                  (autopi/destination ship)
                                  -1 -1))) :t 1 :l 2)
      :map
      (canvas/paint
        canvas
        (gui/scrollable-bitmap 
          (sys/visualize (gs/get-universe gs (take 3 path)))
          canvas
          (or (-> gs :console :map :y) 0) 
          (or (-> gs :console :map :x) 0)
          2)
        :t 1 :l 1)
      :gmap
      (canvas/paint
        canvas
        (gui/scrollable-bitmap
          (space/visualize (gs/get-universe gs [:space]))
          canvas
          (or (-> gs :console :map :y) 0) 
          (or (-> gs :console :map :x) 0)
          2)
        :t 1 :l 1)
      :gate
      (do
        (canvas/paint
        canvas
        (gui/menu-vis
          (map #(:name (first %))
               (apply (partial space/get-near-systems (gs/get-universe gs [:space]) 1)
                      (take 2 (drop 1 (-> gs :console :path))))))
        :t 1 :l 2))
      
      )))
