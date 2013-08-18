(ns sss.universe.space.core
  (:require [sss.universe.system.core :as system]
            [sss.universe.random :as rnd]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]))

(defn gen-space []
  (reduce
    (fn [systems rowi]
      (conj systems
            (reduce
              (fn [row celli]
                (conj row
                      (system/gen-system row)))
              []
              (range (rnd/r 6 12)))))
    []
    (range (rnd/r 6 12))))

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

(defn pointed-data
  [space & {:keys [margin-x margin-y paint-cb] 
            :or {margin-x 10 margin-y 4 paint-cb (fn [c & _] c)}}]
  (let [initconj (fn [v d]
                   (if (coll? v)
                     (conj v d)
                     [d]))
        data {}]
    (reduce
      (fn [d [row y]]
        (reduce
          (fn [d [system x]]
            (update-in d
                       [(+ 2 (-> system :offset first) (* x margin-x))
                        (+ 2 (-> system :offset second) (* y margin-y))
                        :systems]
                       initconj 
                       (-> system
                           (assoc :rx x)
                           (assoc :ry y))))
          d
          (map vector row (range))))
      data
      (map vector space (range)))))

(defn visualize
  [data & {:keys [margin-x margin-y paint-cb] 
            :or {margin-x 1 margin-y 1 paint-cb (fn [c & _] c)}
            :as kv}]
  (reduce
    (fn [c [x row]]
      (reduce
        (fn [c [y kv]]
          (reduce
            (fn [c [k v]]
              (reduce
                (fn [c i]
                  (paint-cb
                    (can/in-paint
                      c
                      ((bm/bitmap "`yellow:*") :t y :l x)
                      ((gr/string (str "`magenta:" (:name i))) :t (dec y) :l (- x 1)))
                  x y (:rx i) (:ry i) i))
                c
                v))
            c
            kv))
        c
        row))
    (can/canvas 500 500)
    data))


(defn visualize2
  [space & {:keys [margin-x margin-y paint-cb] 
            :or {margin-x 6 margin-y 1 paint-cb (fn [c & _] c)}}]
  (let [canvas (can/canvas 120 120)]
    (reduce
      (fn [c row-y]
        (let [y (inc (second row-y))
              row (first row-y)]
          (reduce
            (fn [c system-x]
              (let [x (inc (second system-x))
                    system (first system-x)
                    fx (+ (first (:offset system)) x 5)
                    fy (+ (second (:offset system)) y 5)]
                (paint-cb (can/in-paint 
                            c
                            ((bm/bitmap "*") 
                             :l (* fx margin-x) 
                             :t (* fy margin-y))
                            ((gr/string (:name system)) 
                             :l (* fx margin-x) 
                             :t (dec (* fy margin-y)))) 
                          (* fx margin-x) 
                          (* fy margin-y)
                          (second system-x)
                          (second row-y)
                          system)))
            c
            (map vector row (range)))))
      canvas
      (map vector space (range)))))
