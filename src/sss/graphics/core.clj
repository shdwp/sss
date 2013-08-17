(ns sss.graphics.core
  (:require [sss.graphics.canvas :refer :all]
            [sss.graphics.bitmap :refer :all]))

(defn line [bx by ch]
  (let [bitmap (map-indexed
                 (fn [y row]
                   (assoc (vec row) (-> (* (/ bx by) y) int) ch))
                 (repeat by (repeat bx " ")))
        bitmap (rev (map-indexed 
                      (fn [x col]
                        (if (contains? col ch)
                          col
                          (assoc (vec col) (-> (* (/ by bx) x) int) ch)))
                      (rev bitmap)))]
    bitmap))

(defn string [s]
  (bitmap s))

(defn rect [bx by ch]
  (map #(if (or (zero? %) (= (dec by) %)) 
                        (repeat bx ch) 
                        (concat (list ch) (repeat (- bx 2) nil) (list ch)))
                     (range by)))

(defn text-buffer [lines w h]
  (reverse (map #(str %) (take h (reverse lines)))))

(defn split-str-at [limit & strings]
  (reduce
    (fn [res s]
      (concat res 
              (reduce
                (fn [splitted ch]
                  (if (>= (count (last splitted)) limit)
                    (conj (vec splitted) (str ch))
                    (conj (vec (butlast splitted)) (str (last splitted) ch))))
                []
                s)))
    []
    strings))
