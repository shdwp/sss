(ns sss.game.console.cc 
  "Command center - console with map, g(lobal)map and autopilot"
  (:require [sss.graphics.canvas :as canvas]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.gui.core :as gui]
            [sss.game.gamestate :as gs]
            [sss.universe.core :as uni]
            [sss.universe.system.core :as sys]
            [sss.universe.planet.core :as planet]
            [sss.universe.space.core :as space]
            [sss.ship.autopilot :as autopi]
            [taoensso.timbre :refer [spy]]
            ))

(defn set-mode [gs mode]
  (assoc-in gs (gs/gd-data-path :cc-console :mode) mode))

(defn gate-menu 
  [gs]
  (let [ship-path (take 5 (gs/gd-data gs :cc-console :path))
        system-path (take 3 ship-path)]
    (if (= (gs/gd-data gs :cc-console :mode) :gate)
      (gui/menu-sel gs 
                    (map 
                      second
                      (space/get-near-systems (gs/get-universe gs [:space])
                                              1
                                              (nth system-path 2)
                                              (nth system-path 1)))
                    #(-> %1
                         (gs/update-universe (concat system-path [:gate :queue])
                                             conj [ship-path %2])
                         (gs/turn)
                         (gs/gd-pop :console)
                         ))
      gs)))

(defn info-on [object]
  (flatten 
    (reduce
      (fn [s [k v]]
        (conj s (reduce
                  (fn [s o]
                    (conj s 
                          (case k
                            :systems (sys/system-summary o)
                            :planets (planet/planet-summary o)
                            :gates (sys/gate-summary o)
                            :stars (sys/system-summary o)
                            {:unknown 404})))
                  []
                  v)))
      []
      object)))

(defn gen-maps-data [gs]
  (if-not (gs/gd-data gs :cc-console :maps)
    (gs/gd-set-data gs :cc-console :maps :gmap :data 
                    (space/pointed-data (gs/get-universe gs [:space])))
    gs))

(defn gen-maps-bitmap [gs]
  (if-not (gs/gd-data gs :cc-console :maps :gmap :bitmap)
    (gs/gd-set-data 
      gs 
      :cc-console :maps :gmap :bitmap
      (space/visualize
        (gs/gd-data gs :cc-console :maps :gmap :data)
        :paint-callback (fn [c fx fy x y s]
                          (if (>= (uni/compare-paths [:space y x] 
                                                     (:actor-path gs)) 3)
                            (canvas/paint c (bm/bitmap "@") :t fy :l (dec fx))
                            c))))
    gs))

(defn pointable-map [gs x y]
  (let [data (sys/pointed-data
               (gs/get-universe gs (take 3 (gs/gd-data gs :cc-console :path))) 
               (:turn gs))]
    (gs/gd-set-data 
      gs 
      :cc-console :maps :map :selected
      (get-in data [x y]))))

(defn update [gs -gs]
  (-> gs
      gen-maps-data
      gen-maps-bitmap
      gate-menu
      (gui/mode-toggle (gs/gd-data-path :cc-console :mode) \m :info :map)
      (gui/mode-toggle (gs/gd-data-path :cc-console :mode) \M :info :gmap)
      (gui/mode-toggle (gs/gd-data-path :cc-console :mode) \g :info :gate)

      (gui/pointable-in-modes (gs/gd-data-path :cc-console :mode)
                              [:gmap]
                              (canvas/canvas 50 30)
                              (or (gs/gd-data gs :cc-console :map :x) 0) 
                              (or (gs/gd-data gs :cc-console :map :y) 0)
                              (fn [g x y]
                                (gs/gd-set-data
                                  g
                                  :cc-console :maps :gmap :selected
                                  (get-in (gs/gd-data gs :cc-console :maps :gmap :data) 
                                          [x y]))))
      (gui/pointable-in-modes (gs/gd-data-path :cc-console :mode)
                              [:map]
                              (canvas/canvas 50 30) ;; @TODO
                              (or (gs/gd-data gs :cc-console :map :x) 0) 
                              (or (gs/gd-data gs :cc-console :map :y) 0)
                              pointable-map)
      (gui/scrollable-in-modes (gs/gd-data-path :cc-console :mode)
                               [:gmap :map] 
                               (gs/gd-data-path :cc-console :map :x)
                               (gs/gd-data-path :cc-console :map :y))))

(defn paint [canvas gs]
  (let [path (gs/gd-data gs :cc-console :path)
        ship (gs/get-universe gs path)]
    (case (gs/gd-data gs :cc-console :mode)
      :info 
      (canvas/paint 
        canvas 
        (gui/text 
          48
          (str "ship " (:name ship))
          "position"
          (str " " (uni/unipath (:universe gs) 
                                path 
                                (:x ship) 
                                (:y ship)))
          (str "autopilot " (if (autopi/enabled? ship) "on" "off"))
          (str "to " (uni/unipath (:universe gs)
                                  (autopi/destination ship)
                                  -1 -1)))
        :t 1 :l 2)
      :map
      (canvas/in-paint
        canvas
        ((gui/scrollable-pointable-bitmap 
           canvas
           (sys/visualize (gs/get-universe gs (take 3 path)) (:turn gs))
           (or (gs/gd-data gs :cc-console :map :y) 0) 
           (or (gs/gd-data gs :cc-console :map :x) 0)
           2) :t 1 :l 1)
        ((apply gui/text 30 (flatten 
                              (map gr/text-map 
                                   (info-on (gs/gd-data gs :cc-console :maps :map :selected)))))
         :t 1 :l 1)
        )
      :gmap
      (let [info (info-on (gs/gd-data gs :cc-console :maps :gmap :selected))]
        (canvas/in-paint
          canvas
          ((gui/scrollable-pointable-bitmap
             canvas
             (gs/gd-data gs :cc-console :maps :gmap :bitmap)
             (or (gs/gd-data gs :cc-console :map :y) 0) 
             (or (gs/gd-data gs :cc-console :map :x) 0)
             2) :t 1 :l 1)
          ((apply gui/text 30 (flatten (map gr/text-map info)))
           :t 1 :l 1)))
      :gate
      (do
        (canvas/paint
        canvas
        (gui/menu-vis
          (map #(:name (first %))
               (apply (partial space/get-near-systems (gs/get-universe gs [:space]) 1)
                      (take 2 (drop 1 (gs/gd-data gs :cc-console :path))))))
        :t 1 :l 2))
      
      )))

(defn align
  [gs path]
  (gs/gd-align
    gs
    {:name :cc-console 
     :update update
     :paint paint}
    {:path path
     :mode :info}
    ))
