(ns sss.universe.space.core
  (:require [sss.universe.system.core :as system]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]))

(defn space []
  [[(system/system "Abc") (system/system "Bca")]])

(defn get-near-systems [space l x y]
  (filter (comp not nil? first)
          (reduce
            (fn [res yi]
              (concat
                res
                (map (fn [xi]
                       (if-not (and (= x xi) (= y yi))
                         [(get-in space [xi yi]) [xi yi]]))
                     (range (- x l) (+ x l 1)))))
            []
            (range (- y l) (+ y l 1)))))


(defn visualize [space]
  (let [canvas (can/canvas 30 20)]
    (reduce
      (fn [c row-y]
        (let [y (inc (second row-y))
              row (first row-y)]
          (reduce
            (fn [c system-x]
              (let [x (inc (second system-x))
                    system (first system-x)]
                (can/in-paint 
                  c
                  ((bm/bitmap "*") :l (* x 5) :t (* y 2))
                  ((gr/string (:name system)) :l (* x 5) :t (dec (* y 2))))))
            c
            (map vector row (range)))))
      canvas
      (map vector space (range)))))
