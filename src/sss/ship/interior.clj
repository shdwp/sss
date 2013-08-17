(ns sss.ship.interior
  (:require [sss.ship.geom :refer :all]
            [sss.tile.ship-material :as m]
            [sss.tile.core :as tile]
            [sss.graphics.bitmap :refer [bitmap]]))

(defn add-mixin [ship x y bitmap]
  (assoc ship :mixins
         (conj (:mixins ship) {:x x :y y :bitmap bitmap})))

(defn make-door [ship side a b]
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
    (add-mixin ship x y (bitmap (tile/tiles (m/metal-rusty-door))))))

;; 00 01 02 03
;; 10 11 12 13 
;; 20 21 22 33
;; 
;;    01 02 03 04 
;;    10 11 12 14
;;    20 21 22 24
