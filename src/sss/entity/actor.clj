(ns sss.entity.actor
  (:require [sss.graphics.bitmap :as bitmap]))


(defn actor [& [x y]]
  {:x (or x 0) :y (or y 0) :bitmap (bitmap/bitmap "@")})

