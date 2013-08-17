(ns sss.universe.system.core
  (:require [sss.graphics.canvas :as can]
            [sss.graphics.core :as gr]
            [sss.graphics.bitmap :as bm]))

(defn system [n]
  {:name n
   :gate {:x 10 :y 10}
   :ships []
   :planets []
   :stations []})

(defn visualize [system]
  (let [canvas (can/canvas 30 20)
        apply-gate (fn [c s]
                     (can/paint c (bm/bitmap "G") :l (-> s :gate :x) :t (-> s :gate :y)))
        apply-ships (fn [c s]
                      (reduce
                        (fn [c ship]
                          (can/paint c (bm/bitmap "0") :l (-> ship :x) :t (-> ship :y)))
                        c
                        (:ships s)))] 
    (-> canvas
        (apply-gate system)
        (apply-ships system))))


