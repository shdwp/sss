(ns sss.universe.space.core
  "Space - part of universe representing space"
  (:require [sss.universe.system.core :as system]
            [sss.universe.random :as rnd]
            [sss.universe.social.politics :as politics]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]
            [sss.universe.util :refer :all]))

(def rows-bounds [12 24])
(def cols-bounds [12 24])

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
            :or {margin-x 6 margin-y 1 paint-cb (fn [c & _] c)}}]
  (let [global-offset-x 2
        global-offset-y 2]
    (reduce-space
      (fn [d x y system]
        (update-in
          d
          [(+ global-offset-y (-> system :offset second) (* y margin-y))]
          (fn [node]
            (update-in 
              (if (map? node) node (sorted-map))
              [(+ global-offset-x (-> system :offset first) (* x margin-x)) :systems]
              initconj
              (-> system
                  (assoc :rx x)
                  (assoc :ry y)))
            )))
      (sorted-map)
      space)))

(defn visualize
  "Visualize pointed data ~data, applying ~paint-callback to every item.
  Paint callback - fn with [canvas, paint-x paint-y system-x system-y system]
  @return canvas"
  [universe data & {:keys [paint-callback] :or [paint-callback (fn [c & _] c)]}]
  (reduce-pdata 
    (fn [c x y k o]
      (let [get-color (fn [kw] (let [s (str kw)] (.substring s 1 (count s))))
            union (politics/system-owner universe o)
            color (if (not= union :unoccupied) (get-color (:color union)) "black")
            line (apply str (repeat 7 \ ))]
        (paint-callback
          (can/in-paint
            c
            ((gr/string (str "~" color ":" line)) :t y :l (- x 1))
            ((gr/string (str "~" color ":" line)) :t (inc y) :l (- x 3))
            ((gr/string (str "`white:~" color ":*")) :t y :l x))
          x y (:rx o) (:ry o) o)
        ))
    (can/canvas 500 500)
    data))

