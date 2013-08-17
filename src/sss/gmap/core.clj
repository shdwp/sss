(ns sss.gmap.core
  (:require 
    [sss.graphics.bitmap :as bitmap]
    [sss.tile.core :as tile]
    [sss.graphics.canvas :refer [canvas paint]]))

(defn gmap [w h]
  {:w w
   :h h
   :map (canvas w h)
   :manager []
   })

(defn put [gm bitmap x y]
  (assoc gm :map (paint (:map gm) bitmap :t y :l x)))

(defn put-tile [gm tile x y]
  (put gm (bitmap/bitmap (tile/tiles tile)) x y))

(defn as-canvas
  "Get gmap as canvas"
  [gm]
  (:map gm))

(defn char-at [gm x y]
  (bitmap/char-at (as-canvas gm) x y))

(defn dispatch-xy [gm x y f & args]
  (update-in gm [:manager] conj (list (list x y) (list f args))))

(defn dispatch-test [gm tfn f & args]
  (update-in gm [:manager] conj (list tfn (list f args))))

(defn run-dispatcher [gm]
  (assoc 
    gm 
    :map 
    (map-indexed
      (fn [y row]
        (map-indexed
          (fn [x ch]
            (or
              (reduce (fn [c m]
                        (let [t (first m)
                              f (second m)]
                          (cond
                            (and (ifn? t) (t ch))
                            (apply (first f) x y c (second f))
                            (list? t)
                            (if (= (list x y) t) (apply (first f) x y c (second f)))
                            :else c)))
                      ch
                      (:manager gm))
              ch))
          row))
      (as-canvas gm))))


