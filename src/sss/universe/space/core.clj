(ns sss.universe.space.core
  "Space - part of universe representing space"
  (:require [sss.universe.system.core :as system]
            [sss.universe.random :as rnd]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]
            [sss.universe.util :refer :all]))

(def rows-bounds [6 10])
(def cols-bounds [6 10])

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
  "Get pointed-data of ~space, @return pointed data"
  [space & {:keys [margin-x margin-y paint-cb] 
            :or {margin-x 10 margin-y 4 paint-cb (fn [c & _] c)}}]
  (reduce-space
    (fn [d x y system]
      (update-in 
        d
        [(+ 2 (-> system :offset first) (* x margin-x))
         (+ 2 (-> system :offset second) (* y margin-y))
         :systems]
        initconj 
        (-> system
            (assoc :rx x)
            (assoc :ry y))))
    {}
    space))


(defn visualize
  "Visualize pointed data ~data, applying ~paint-callback to every item.
  Paint callback - fn with [canvas, paint-x paint-y system-x system-y system]
  @return canvas"
  [data & {:keys [paint-callback] :or [paint-callback (fn [c & _] c)]}]
  (reduce-pdata 
    (fn [c x y k o]
      (paint-callback
        (can/in-paint
          c
          ((bm/bitmap "`yellow:*") :t y :l x)
          ((gr/string (str "`magenta:" (:name o))) :t (dec y) :l (- x 1)))
        x y (:rx o) (:ry o) o))
    (can/canvas 500 500)
    data))

