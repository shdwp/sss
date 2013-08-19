(ns sss.entity.core
  "Entity - quiet dynamic thing in universe (great description)")

(defn bitmap 
  "Get ~entity's bitmap"
  [entity]
  (:bitmap entity))

(defn move 
  "Move ~entity with ~x and ~y"
  [entity x y]
  (assoc (assoc entity :y (+ (:y entity) y)) :x (+ (:x entity) x)))
