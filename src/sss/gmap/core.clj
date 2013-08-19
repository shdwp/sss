(ns sss.gmap.core
  "Gmap - game map (tilemap)" 
  (:require 
    [sss.graphics.bitmap :as bitmap]
    [sss.tile.core :as tile]
    [sss.graphics.canvas :refer [canvas paint]]))

(defn gmap 
  "Create gmap (tilemap) with ~w(idth) and ~h(eight)"
  [w h]
  {:w w
   :h h
   :map (canvas w h)
   :manager []
   })

(defn put 
  "Put ~bitmap on ~gm(ap) on ~x and ~y"
  [gm bitmap x y]
  (assoc gm :map (paint (:map gm) bitmap :t y :l x)))

(defn put-tile 
  "Put ~tile on ~gm(ap) on ~x and ~y"
  [gm tile x y]
  (put gm (bitmap/bitmap (tile/tiles tile)) x y))

(defn as-bitmap
  "Get ~gm(ap) as bitmap"
  [gm]
  (:map gm))

(defn char-at 
  "Get character at ~x and ~y at ~gm(ap)"
  [gm x y]
  (bitmap/char-at (as-bitmap gm) x y))

;; Currently not needed \/

(defn dispatch-xy 
  [gm x y f & args]
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
      (as-bitmap gm))))


