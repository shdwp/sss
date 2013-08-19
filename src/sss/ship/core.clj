(ns sss.ship.core
  "Ship - object in :ships part of system representing spaceship."
  (:require [sss.gmap.core :as gmap]
            [sss.ship.form :as form]
            [sss.ship.gen :refer :all]))

(defn -ship [] {:name "testship" 
                :x 0 :y 0
                :systems {:autopilot {:enabled true 
                                      :to [:space 0 1]}}
                :size [3 2]
                :module-size [7 6]
                :mixins []
                :scheme [nil
                         {:name "comm" :x 1 :y 0 :gen #(gen-cc %)} 
                         nil
                         ,
                         {:name "living" :x 0 :y 1 :gen #(gen-living %)}
                         {:name "engine" :x 1 :y 1 :gen #(gen-engine %)} 
                         {:name "hold" :x 2 :y 1 :gen #(gen-hold %)} 
                         ]})

(defn apply-form [gm ship]
  (gmap/put
    gm
    (form/gen-form ship)
    0 
    0))

(defn apply-mixins [gm ship]
  (reduce
    (fn [gm mixin]
      (gmap/put gm (:bitmap mixin) (:x mixin) (:y mixin)))
    gm
    (:mixins ship)))

(defn apply-modules [gm ship]
  (reduce #(do 
             (gmap/put 
               %1 
               (:map (second %2))
               (* (:x (second %2)) (first (:module-size ship)))
               (* (:y (second %2)) (second (:module-size ship)))))
          gm
          (filter 
            #(not (nil? (second %))) 
            (map-indexed #(vector %1 %2) (:scheme ship)))))

(defn gen-map [-ship]
  (let [ship -ship
        gm (gmap/gmap 
             (inc (* (first (:size ship)) (first (:module-size ship))))
             (inc (* (second (:size ship)) (second (:module-size ship)))))]
    (-> gm
        (apply-form ship)
        (apply-modules ship)
        (apply-mixins ship)
        )))
