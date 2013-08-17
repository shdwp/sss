(ns sss.graphics.bitmap)

;; Bitmap - 2-level vector to put on canvas (simple sprite)

(defn bitmap [& rows]
  rows)

(defn char-at [bitmap x y]
  (let [row (nth bitmap y '())]
    (cond
      (seq? row) (nth row x nil)
      (string? row) (get row x))))

(defn rev [[& bitmap]]
  (vec (apply (partial map (fn [& args] (vec args))) bitmap)))
