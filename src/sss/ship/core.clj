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
                         {:name "comm" :x 1 :y 0 :gen 'sss.ship.gen/gen-cc} 
                         nil
                         ,
                         {:name "living" :x 0 :y 1 :gen 'sss.ship.gen/gen-living}
                         {:name "engine" :x 1 :y 1 :gen 'sss.ship.gen/gen-engine} 
                         {:name "hold" :x 2 :y 1 :gen 'sss.ship.gen/gen-hold} 
                         ]})

(defn apply-form 
  "Apply form (background) of ~ship to ~gm(ap)"
  [gm ship]
  (gmap/put
    gm
    (form/gen-form ship)
    0 
    0))

(defn map-mixin [mixin ship]
  (apply (resolve (:gen mixin)) ship []))

(defn apply-mixins 
  "Apply mixins (doors, etc) of ~ship to ~gm(ap)"
  [gm ship]
  (reduce
    (fn [gm mixin]
      (gmap/put gm (map-mixin mixin ship) (:x mixin) (:y mixin)))
    gm
    (:mixins ship)))

(defn get-enumerated-not-nil-modules [ship]
  (filter (comp not nil? second)
          (map-indexed vector (:scheme ship))))
  
(defn map-module [module ship]
  (apply (resolve (:gen module)) [ship]))

(defn apply-modules 
  "Apply modules of ~ship to ~gm(ap)"
  [gm ship]
  (reduce #(let [module (second %2)] 
             (gmap/put 
               %1 
               (map-module module ship)
               (* (:x module) (first (:module-size ship)))
               (* (:y module) (second (:module-size ship)))))
          gm
          (get-enumerated-not-nil-modules ship)))

(defn gen-map 
  "Generate ~ship gmap"
  [-ship]
  (let [ship -ship
        gm (gmap/gmap 
             (inc (* (first (:size ship)) (first (:module-size ship))))
             (inc (* (second (:size ship)) (second (:module-size ship)))))]
    (-> gm
        (apply-form ship)
        (apply-modules ship)
        (apply-mixins ship)
        )))

(defn move [ship x y]
  (-> ship
      (update-in [:x] #(+ % x))
      (update-in [:y] #(+ % y))))
