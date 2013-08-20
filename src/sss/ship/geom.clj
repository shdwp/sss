(ns sss.ship.geom
  )

(defn intersects 
  "Is module ~a intersects with ~b?"
  [a b]
  (let [xd (- (:x a) (:x b))
        xp (if (pos? xd) :left :right)
        yd (- (:y a) (:y b))
        yp (if (pos? yd) :top :bottom)]
    (cond
      (and (= 1 (Math/abs xd)) (= 1 (Math/abs yd))) nil
      (and (zero? (Math/abs yd)) (= 1 (Math/abs xd))) xp
      (and (zero? (Math/abs xd)) (= 1 (Math/abs yd))) yp
      :else nil)))

(defn mod-abs-pos 
  "Get ~m(odule) absolute position of ~coord (:x or :y) on ~ship"
  [coord ship m]
  (*  
    (if (= :x coord) (first (:module-size ship)) (second (:module-size ship))) 
    (coord m)))

(defn mod-absx 
  "Shortcut to mod-abs-pos, (mod-absx ship module)"
  [& args]
  (apply (partial mod-abs-pos :x) args))

(defn mod-absy 
  "Shortcut to mod-abs-pos, (mod-absx ship module)"
  [& args]
  (apply (partial mod-abs-pos :y) args))

(defn mod-w 
  "Get modules width at ~ship"
  [ship]
  (first (:module-size ship)))

(defn mod-h 
  "Get modules height at ~ship"
  [ship]
  (second (:module-size ship)))

(defn ship-w 
  "Get ~ship width"
  [ship]
  (* (first (:size ship)) (first (:module-size ship))))

(defn ship-h 
  "Get ~ship height"
  [ship]
  (* (second (:size ship)) (second (:module-size ship))))
