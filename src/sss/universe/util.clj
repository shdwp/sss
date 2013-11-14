(ns sss.universe.util
  (:require [sss.graphics.canvas :as can]
            [sss.universe.random :as rnd]))

(defn initconj [v d]
  (if (coll? v)
    (conj v d)
    [d]))

(defn mapspace [f space]
  (vec (map-indexed
         (fn [y row]
           (vec (map-indexed
                  (fn [x cell]
                    (f x y cell))
                  row)))
         space)))

(defn move-with 
  "Move smth's angle position with ~speed"
  [start speed turn]
  (let [moved (double (+ (* turn speed) start))
        years (int (/ moved 360))]
    (- moved (* years 360))))

(defn reduce-space [f accum space]
  (reduce
    (fn [d [row y]]
      (reduce
        (fn [d [system x]]
          (f d x y system))
        d
        (map vector row (range))))
    accum
    (map vector space (range))))

(defn reduce-pdata [f accum pdata]
  (reduce
    (fn [c [y row]]
      (reduce
        (fn [c [x kv]]
          (reduce
            (fn [c [k v]]
              (reduce
                (fn [c i]
                  (f c x y k i))
                c
                v))
            c
            kv))
        c
        row))
    accum
    pdata))
