(ns sss.graphics.viewport
  (:require [sss.graphics.canvas :refer [crop]]))

(defn nth-in [col ks & nope]
  (if (coll? col)
    (nth-in (nth col (first ks) nope) (rest ks))
    col))

(defn view! [canvas w h & {:keys [t l b r] :or [0 0 0 0]}]
  (let [w (if (neg? w) (count (first canvas)) w)
        h (if (neg? h) (count canvas) h)]
    (mapv
      (fn [row]
        (mapv #(print %) row)
        (println))
      (crop canvas w h t l b r))
    true))

(defn view-bitmap [canvas w h & {:keys [t l b r] :or [0 0 0 0]}]
  (let [w (if (neg? w) (count (first canvas)) w)
        h (if (neg? h) (count canvas) h)]
    (crop canvas w h t l b r)))

(defn view-bitmap-centered [canvas w h x y]
  (let [t (double (+ (- y) (/ h 2)))
        l (double (+ (- x) (/ w 2)))]
    (view-bitmap canvas w h :t t :l l)))

