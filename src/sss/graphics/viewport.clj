(ns sss.graphics.viewport
  "Various functions to make bitmap viewable (positioned and cropped)"
  (:require [sss.graphics.canvas :refer [crop]]))

;; @TODO: clean up
(defn nth-in 
  "nth-in, just like get-in"
  [col ks & nope]
  (if (coll? col)
    (nth-in (nth col (first ks) nope) (rest ks))
    col))

(defn view-bitmap 
  "Crop ~bitmap to ~w(idht) and ~h(eight) and move it with ~t ~l or ~b ~r"
  [bitmap w h & {:keys [t l b r] :or [t 0 l 0]}]
  (let [w (if (neg? w) (count (first bitmap)) w)
        h (if (neg? h) (count bitmap) h)]
    (crop bitmap w h t l b r)))

(defn view-bitmap-centered 
  "(view-bitmap ~bitmap ~w ~h), but center it to ~x ~y"
  [bitmap w h x y]
  (let [t (double (+ (- y) (/ h 2)))
        l (double (+ (- x) (/ w 2)))]
    (view-bitmap bitmap w h :t (- t) :l (- l))))

