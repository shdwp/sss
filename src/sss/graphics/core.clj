(ns sss.graphics.core
  (:require [sss.graphics.canvas :refer :all]
            [clojure.string :refer [join]]
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
                  (if (or (if (= limit -1) false (>= (count (last splitted)) limit))
                          (nil? ch) 
                          (= ch \newline))
                    (conj (vec splitted) (str ch))
                    (conj (vec (butlast splitted)) (str (last splitted) ch))))
                []
                s)))
    []
    strings))

(defn text-map [m]
  (reduce
    (fn [s [k v]]
      (conj s (str (join (drop 1 (str k))) ": " v)))
    []
    m))
