(ns sss.graphics.core
  "General functions for creating bitmaps of some primitives"
  (:require [sss.graphics.canvas :refer :all]
            [clojure.string :refer [join]]
            [sss.graphics.bitmap :refer :all]))

(defn line 
  "Create bitmap representing a line from (0;0) to (~bx;~by) and filled with ~ch(ar)"
  [bx by ch]
  (let [bitmap (map-indexed
                 (fn [y row]
                   (assoc (vec row) (-> (* (/ bx by) y) int) ch))
                 (repeat by (repeat bx " ")))
        bitmap (transpose (map-indexed 
                      (fn [x col]
                        (if (contains? col ch)
                          col
                          (assoc (vec col) (-> (* (/ by bx) x) int) ch)))
                      (transpose bitmap)))]
    bitmap))

(defn string 
  "Create bitmap representing a ~s(tring)"
  [s]
  (bitmap s))

(defn rect 
  "Create bitmap representing rectangle from (0;0) to (~bx;~by), filled with ~ch(ar)"
  [bx by ch]
  (map #(if (or (zero? %) (= (dec by) %)) 
                        (repeat bx ch) 
                        (concat (list ch) (repeat (- bx 2) nil) (list ch)))
                     (range by)))

(defn text [s]
  (apply bitmap (vec (.split s "\n"))))

;; @TODO: move it somewhere \/

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
