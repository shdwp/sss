(ns sss.entity.actor
  "Actor - entity representing current playable entity"
  (:require [sss.graphics.bitmap :as bitmap]))


(defn actor 
  "Create actor (you may provide starting ~x and ~y)"
  [& [x y]]
  {:x (or x 0) :y (or y 0) :bitmap (bitmap/bitmap "@")})

