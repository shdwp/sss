(ns sss.universe.system.core
  (:require [sss.graphics.canvas :as can]
            [sss.universe.random :as rnd]
            [sss.universe.social.lingvo :as lin]
            [sss.universe.planet.core :as planet]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]))

(defn gen-system [row]
  (-> 
    {:name (rnd/unique row :name lin/gen-star-name)
     :gate {:x (rnd/r 2 18) :y (rnd/r 2 18)}
     :star [10 10]
     :track-size 2
     :ships []
     :planets []
     :stations []
     :offset [(rnd/r -2 2) (rnd/r -1 1)]}
    (#(assoc % :planets (planet/gen-planets %)))
    ))

(defn system-summary [system]
  {:name (:name system)
   :planets (count (:planets system))
   :ships (count (:ships system))})

(defn gate-summary [gate]
  {:description "gates are only items that have description, you should care of it"})

(defn pointed-data [system tick]
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
                                    a ((:pos planet) tick)
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
        apply-star (fn [d s]
                     (update-in d (conj (:star s) :stars) initconj s))]
    (-> data
        (apply-star system)
        (apply-planets system)
        (apply-gate system)
        (apply-ships system))))

(defn visualize [system tick]
  (reduce
    (fn [c [x row]]
      (reduce
        (fn [c [y items]]
          (reduce
            (fn [c [k v]]
              (reduce
                (fn [c i]
                  (can/paint c (case k
                                 :ships (bm/bitmap "`blue:0")
                                 :gates (bm/bitmap "`blue:G")
                                 :stars (bm/bitmap "`yellow:*")
                                 :planets (bm/bitmap "`cyan:o")
                                 (bm/bitmap "?"))
                             :t y :l x))
                c
                v))
            c
            items))
        c row))
    (can/canvas 120 120)
    (pointed-data system tick)))
