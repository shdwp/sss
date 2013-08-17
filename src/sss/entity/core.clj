(ns sss.entity.core
  )

(defn bitmap [entity]
  (:bitmap entity))

(defn move [entity x y]
  (assoc (assoc entity :y (+ (:y entity) y)) :x (+ (:x entity) x)))
