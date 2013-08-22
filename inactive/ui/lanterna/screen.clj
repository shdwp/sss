(ns sss.ui.lanterna.screen
  (:require [sss.graphics.viewport :refer [nth-in]]
            [taoensso.timbre.profiling :refer [p profile]]
            [sss.graphics.core :as graphics]
            [sss.tile.core :as tile]
            [sss.tile.ship-material :as m]
            [clojure.data :refer [diff]]))

(defn get-char [buffer x y]
  (if (>= y (count buffer))
    nil
    (let [row (nth buffer y)]
      (if (>= x (count row))
        nil
        (nth row x)))))

(defn paint [term x y ch]
  (.moveCursor term x y)
  (.putCharacter term (tile/get-character ch)))

(defn refresh [term old-buffer buffer]
  (doall (map
    (fn [y]
      (doall (map
        (fn [x]
          (let [newch (get-char buffer x y)
                oldch (get-char old-buffer x y)]
            (if (not= newch oldch)
              (paint term x y newch))
            (if (and (nil? newch) (not (nil? oldch)))
              (paint term x y \space))))
        (take (.. term getTerminalSize getColumns) (range)))))
    (take (.. term getTerminalSize getRows) (range))))
  buffer)

(defn refresh2 [term old-buffer buffer]
  (let [size (.getTerminalSize term)]
    (for [y (take (.getRows size) (range))]
      (for [x (take (.getColumns size) (range))]
        (let [newch (nth-in buffer [y x])
              oldch (nth-in old-buffer [y x])]
          (if (not= newch oldch)
            (paint term x y newch))
          (if (and (nil? newch) (not (nil? oldch)))
            (paint term x y \space))))))
  buffer)

(defn refresh-diff [term old-buffer buffer]
  (map-indexed
    (fn [y row]
      (map-indexed
        (fn [x ch]
          (paint term x y ch))
        row))
    (second (diff old-buffer buffer))))

(defn refresh-dummie [term _ buffer]
  (doall (map-indexed
           (fn [y row]
             (doall (map-indexed
                      (fn [x ch]
                        (paint term x y ch))
                      row)))
           buffer))
  buffer)

(defn refresh-bounded [term old-buffer buffer]
  (doall (map
           (fn [y oldrow row]
             (doall (map
                      (fn [x ch oldch]
                        (paint term x y ch))
                      (range)
                      oldrow
                      row)))
           (range)
           old-buffer
           buffer))
  buffer)
