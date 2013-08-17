(ns sss.graphics.canvas
  (:require [sss.graphics.bitmap :refer [char-at]]))

;; Canvas - 2 level vector contains characters to paint

(defn canvas [w h]
  (repeat h (repeat w " ")))

(defn w [canvas]
  (if (empty? canvas)
    0
    (count (first canvas))))

(defn h [canvas]
  (count canvas))

(defn bitmap-paint-cords [y x t l]
  [(- x l) (- y t)])

(defn paint-padding [canvas bitmap t l]
  (map-indexed 
    (fn [y row]
      (map-indexed
        (fn [x px]
          (let [bpx (apply (partial char-at bitmap) (bitmap-paint-cords y x t l))]
            (if (nil? bpx)
              px
              bpx)))
        row))
    canvas))

(defn paint 
  ([canvas bitmap & {:keys [t l b r] :or [nil nil nil nil] :as padding}]
   ;; @TODO: warn about nil padding
   (cond
     (and (nil? b) (nil? r)) (paint-padding canvas bitmap t l)
     (and (nil? t) (nil? l)) (paint-padding canvas bitmap 
                                    (- b (count bitmap)) 
                                    (- r (count (first bitmap))))
     :else nil)))

(defmacro in-paint [canvas & forms]
  (let [forms# (map #(concat (list paint) %) forms)]
    `(-> ~canvas ~@forms#)))

(defn crop [canvas w h t l b r]
  (let [ta (if (pos? t) t 0)
        la (if (pos? l) l 0)

        t (if (nil? t) 0 t)
        l (if (nil? l) 0 l)
        t (if (pos? t) 0 t)
        l (if (pos? l) 0 l)


        h (+ (if (nil? h) (count canvas) (- h t)) ta)
        w (+ (if (nil? w) (count (first canvas)) (- w l)) la)
        
        canvas (if (pos? ta) (concat (repeat ta " ") canvas) canvas)
        canvas (if (pos? la) (map #(concat (repeat la " ") %) canvas) canvas)]
  (map #(take w (drop (-> l Math/abs int) %)) 
       (take h (drop (-> t Math/abs int) canvas)))))

