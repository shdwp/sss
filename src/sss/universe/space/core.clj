(ns sss.universe.space.core
  "Space - part of universe representing space"
  (:require [sss.universe.system.core :as system]
            [sss.universe.random :as rnd]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]))

(def rows-bounds [6 12])
(def cols-bounds [6 12])

(defn gen-row []
  "Generate row of space"
  (reduce
    (fn [row _]
      (conj row
            (system/gen-system row)))
    []
    (range (apply rnd/r cols-bounds))))

(defn gen-space 
  "Generate space by rows, threaded (1 thread per row)"
  []
  (let [result (agent [])
        rows (apply rnd/r rows-bounds)]
    (mapv
      (fn [i]
        (-> #(send result conj (gen-row))
            Thread.
            .start))
      (range rows))
    (while (< (count @result) rows)
      (Thread/sleep 100))
    @result))

(defn old-gen-space 
  "Generate space"
  []
  (reduce
    (fn [systems rowi]
      (conj systems
            (gen-row)))
    []
    (range (apply rnd/r rows-bounds))))

(defn get-near-systems 
  "Get systems from ~space near (< ~l(ength)) ~x ~y"
  [space l x y]
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
  "Get pointed-data of ~space"
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
  "Visualize pointed-data ~data"
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

