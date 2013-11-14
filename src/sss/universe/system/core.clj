(ns sss.universe.system.core
  "System - space component representing star system"
  (:require [sss.graphics.canvas :as can]
            [sss.universe.random :as rnd]
            [sss.universe.util :as util]
            [sss.universe.social.lingvo :as lin]
            [sss.universe.planet.core :as planet]
            [sss.universe.station.core :as station]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]))

(defn gen-system 
  "Generate system at space ~row"
  [row]
  (-> 
    {:name (rnd/unique row :name lin/gen-star-name)
     :gate {:x (rnd/r 2 18) :y (rnd/r 2 18)}
     :star [10 10]
     :track-size 1
     :ships []
     :planets []
     :stations []
     :offset [(rnd/r -2 2) (rnd/r 0 0)]}
    (#(assoc % :planets (planet/gen-planets %)))
    (#(assoc % :stations (station/gen-stations %)))
    ))

(defn system-summary 
  "Get ~system summary"
  [system]
  {:name (:name system)
   :planets (count (:planets system))
   :ships (count (:ships system))})

(defn gate-summary 
  "Get ~gate summary"
  [gate]
  {:description "gates are only items that have description, you should care of it"})

(defn pointed-data 
  "Get pointed data of ~system at ~turn"
  [system turn]
  (let [initconj (fn [v d]
                   (if (coll? v)
                     (conj v d)
                     [d]))
        data {}
        apply-gate (fn [d s]
                     (update-in d 
                                [(-> s :gate :x) (-> s :gate :y) :gates] 
                                initconj 
                                (-> s :gate)))
        apply-ships (fn [d s]
                      (reduce
                        (fn [d ship]
                          (update-in d 
                                     [(-> ship :x) (-> ship :y) :ships] 
                                     initconj 
                                     ship))
                        d
                        (:ships s)))
        apply-planets (fn [d s]
                          (reduce
                            (fn [d planet]
                              (let [;; @TODO: you're know what to do
                                    x (* (:track-size system) (:track planet))
                                    y 0
                                    a (util/move-with (:pos planet) (:speed planet) turn)
                                    v [(- (* x (Math/cos a)) (* y (Math/sin a)))
                                       (+ (* x (Math/sin a)) (* y (Math/cos a)))]]
                                (update-in d 
                                           [(int (+ 10 (first v)))
                                            (int (+ 10 (second v)))
                                            :planets]
                                           initconj 
                                           planet)))
                              d
                              (:planets s)))
        apply-stations (fn [d s]
                          (reduce
                            (fn [d station]
                              (let [;; @TODO: you're know what to do
                                    x (* (:track-size system) (:track station))
                                    y 0
                                    a (util/move-with (:pos station) (:speed station) turn)
                                    v [(- (* x (Math/cos a)) (* y (Math/sin a)))
                                       (+ (* x (Math/sin a)) (* y (Math/cos a)))]]
                                (update-in d 
                                           [(int (+ 10 (first v)))
                                            (int (+ 10 (second v)))
                                            :stations]
                                           initconj 
                                           station)))
                              d
                              (:stations s)))
        apply-star (fn [d s]
                     (update-in d (conj (:star s) :stars) initconj s))]
    (-> data
        (apply-star system)
        (apply-planets system)
        (apply-stations system)
        (apply-gate system)
        (apply-ships system))))

;; @TODO
(defn visualize 
  "Visualize pointed data"
  [system turn]
  (reduce
    (fn [c [x row]]
      (reduce
        (fn [c [y items]]
          (reduce
            (fn [c [k v]]
              (reduce
                (fn [c i]
                  (can/paint c (case k
                                 :ships (bm/bitmap "`white:0")
                                 :gates (bm/bitmap "`white:G")
                                 :stars (bm/bitmap "`yellow:*")
                                 :stations (bm/bitmap "`white:S")
                                 :planets (bm/bitmap "`blue:o")
                                 (bm/bitmap "?"))
                             :t y :l x))
                c
                v))
            c
            items))
        c row))
    (can/canvas 120 120)
    (pointed-data system turn)))
