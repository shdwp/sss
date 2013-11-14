(ns sss.ship.interior
  (:require [sss.ship.geom :refer :all]
            [sss.tile.ship-material :as m]
            [sss.tile.core :as tile]
            [sss.graphics.bitmap :refer [bitmap]]))

(defn add-mixin 
  "Add mixin ~bitmap at ~x and ~y to ~ship"
  [ship x y]
  (assoc ship :mixins
         (conj (:mixins ship) {:x x :y y :gen 'sss.ship.gen/gen-rusty-door})))

(defn make-door 
  "Make door at ~ship, located at ~side of ~a"
  [ship side a b]
  (let [mod-absy (partial mod-absy ship)
        mod-absx (partial mod-absx ship)
        mod-w (partial mod-w ship)
        mod-h (partial mod-h ship)

        x (cond 
            (or (= side :top) (= side :bottom)) 
            (+ (mod-absx a)
               (-> (/ (- (mod-absx a) (+ (mod-absx b) (mod-w))) 2)
                   Math/ceil
                   Math/abs
                   int))
            (= side :right) 
            (+ (mod-absx a) (mod-w ship))
            (= side :left)
            (mod-absx a))
        y (cond
            (or (= side :right) (= side :left))
            (+ (mod-absy a)
               (-> (/ (- (mod-absy a) (+ (mod-absy b) (mod-h))) 2)
                   Math/ceil
                   Math/abs
                   int))
            (= side :top)
            (mod-absy a)
            (= side :bottom)
            (+ (mod-absy a) (mod-h ship)))]
    (add-mixin ship x y )))

