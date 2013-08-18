(ns sss.game.console.cc 
  (:require [sss.graphics.canvas :as canvas]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]
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

(defn info-on [object]
  (flatten 
    (reduce
      (fn [s [k v]]
        (conj s (reduce
                  (fn [s o]
                    (conj s 
                          (case k
                            :systems (sys/system-summary o))))
                  []
                  v)))
      []
      object)))

(defn gen-maps-data [gs]
  (if-not (-> gs :console :maps)
    (update-in gs [:console :maps :gmap :data] (fn [_] (space/pointed-data 
                                                         (gs/get-universe gs [:space]))))
    gs))

(defn gen-maps-bitmap [gs]
  (if-not (-> gs :console :maps :gmap :bitmap)
    (update-in 
      gs 
      [:console :maps :gmap :bitmap]
      (fn [_]
        (space/visualize
          (-> gs :console :maps :gmap :data)
          :paint-cb (fn [c fx fy x y s]
                      (if (>= (uni/compare-paths [:space y x] 
                                                 (:actor-path gs)) 3)
                        (canvas/paint c (bm/bitmap "@") :t fy :l (dec fx))
                        c)))))
    gs))

(defn center-map [gs]
  (if-not (-> gs :console :map :x)

  (let [system (reduce
                 (fn [c [x row]]
                   (reduce
                     (fn [c [y d]]
                       (reduce 
                         (fn [c i]
                           (if (>= (uni/compare-paths [:space (:ry i) (:rx i)]
                                                      (:actor-path gs)) 3)
                             (do (prn (:name i))
                               [x y])
                             c))
                         c
                         (:systems d)))
                     c
                     row))
                 []
                 (-> gs :console :maps :gmap :data))
        ]
    (prn system)
    (-> gs
        (update-in [:console :map :y] (fn [_] (-> system second - (/ 2) int)))
        (update-in [:console :map :x] (fn [_] (-> system first - (/ 2) int)))
        ))
    gs))

(defn update [gs -gs]
  (-> gs
      gen-maps-data
      gen-maps-bitmap
      center-map
      gate-menu
      (gui/mode-toggle [:console :mode] \m :info :map)
      (gui/mode-toggle [:console :mode] \M :info :gmap)
      (gui/mode-toggle [:console :mode] \g :info :gate)

      (gui/pointable-in-modes [:console :mode]
                              [:gmap]
                              (-> gs :console :screen)
                              (or (-> gs :console :map :x) 0) 
                              (or (-> gs :console :map :y) 0)
                              (fn [g x y]
                                (update-in 
                                  g
                                  [:console :maps :gmap :selected]
                                  (fn [_] 
                                    (get-in (-> gs :console :maps :gmap :data) [x y])))))
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
      (canvas/in-paint
        canvas
        ((gui/scrollable-pointable-bitmap 
           canvas
           (sys/visualize (gs/get-universe gs (take 3 path)) (:tick gs))
           (or (-> gs :console :map :y) 0) 
           (or (-> gs :console :map :x) 0)
           2) :t 1 :l 1)
        )
      :gmap
      (let [info (info-on (-> gs :console :maps :gmap :selected))]
        (canvas/in-paint
          canvas
          ((gui/text (str (-> gs :console :map :x) (-> gs :console :map :y)))
           :t 0 :l 0)
           ((gui/scrollable-pointable-bitmap
              canvas
              (-> gs :console :maps :gmap :bitmap)
              (or (-> gs :console :map :y) 0) 
              (or (-> gs :console :map :x) 0)
              2) :t 1 :l 1)
          ((apply gui/text (flatten (map gr/text-map info)))
           :t 1 :l 1)))
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
