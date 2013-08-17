(ns sss.ship.geom
  )

(defn intersects [a b]
  (let [xd (- (:x a) (:x b))
        xp (if (pos? xd) :left :right)
        yd (- (:y a) (:y b))
        yp (if (pos? yd) :top :bottom)]
    (cond
      (and (= 1 (Math/abs xd)) (= 1 (Math/abs yd))) nil
      (and (zero? (Math/abs yd)) (= 1 (Math/abs xd))) xp
      (and (zero? (Math/abs xd)) (= 1 (Math/abs yd))) yp
      :else nil)))

(defn mod-abs-pos [coord ship m]
  (*  
    (if (= :x coord) (first (:module-size ship)) (second (:module-size ship))) 
    (coord m)))

(defn mod-absx [& args]
  (apply (partial mod-abs-pos :x) args))

(defn mod-absy [& args]
  (apply (partial mod-abs-pos :y) args))

(defn mod-w [ship]
  (first (:module-size ship)))

(defn mod-h [ship]
  (second (:module-size ship)))

(defn ship-w [ship]
  (* (first (:size ship)) (first (:module-size ship))))

(defn ship-h [ship]
  (* (second (:size ship)) (second (:module-size ship))))
