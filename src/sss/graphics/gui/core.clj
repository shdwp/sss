(ns sss.graphics.gui.core
  (:require [sss.graphics.core :as gr]
            [sss.graphics.viewport :as view]
            [sss.graphics.bitmap :as bm]
            [sss.graphics.canvas :as can]
            [sss.game.gamestate :as gs]))

(defn mode-toggle [gs path k default mode & [cnd]]
  (if (and (gs/get-key-in gs [k]) (if cnd (cnd gs) true))
    (update-in gs path #(if (= % mode) default mode))
    gs))

(defn scrollable [gs cnd xp yp]
  (or 
    (if (cnd gs)
      (if-let [k (gs/get-key-in gs gs/dir-keys)]
        (let [d (gs/direction k)
              x (or (get-in gs xp) 0)
              y (or (get-in gs yp) 0)
              xy (gs/xy-apply-dir d x y)]
          (-> gs
              (update-in xp (fn [_] (first xy)))
              (update-in yp (fn [_] (second xy)))))))
    gs))

(defn scrollable-in-modes [gs value values xp yp]
  (scrollable gs #((set values) (get-in % value)) xp yp))

(defn pointable [gs canvas ox oy getter]
  (let [cursor [(+ (int (dec (/ (can/w canvas) 2))) ox) 
                (+ (int (dec (/ (can/h canvas) 2))) oy)]]
    (apply getter gs cursor)))

(defn pointable-in-modes [gs value values canvas ox oy getter]
  (if ((set values) (get-in gs value))
    (pointable gs canvas ox oy getter)
    gs))

(defn text [w & lines]
  (apply gr/split-str-at w lines))

(defn scrollable-bitmap [bm canvas t l & [border]]
  (view/view-bitmap bm
                    (- (can/w canvas) l (or border 0))
                    (- (can/h canvas) t (or border 0))
                    :t t
                    :l l))

(defn scrollable-pointable-bitmap [canvas bm t l & [border]]
  (let [w (can/w canvas)
        h (can/h canvas)]
    (-> bm
        (view/view-bitmap 
          (- w (or border 0))
          (- h (or border 0))
          :t t
          :l l)
        (can/paint (bm/bitmap "X") :l (int (dec (/ w 2))) :t (int (dec (/ h 2))))
  )))

(def key-choices (map char (range 97 126)))

(defn menu-sel [gs choices on-choice]
  (if-let [k (gs/get-key-in gs key-choices)]
    (on-choice gs (nth choices (- (int k) (int (first key-choices))) nil))
    gs))

(defn menu-vis [choices]
  (apply text -1 (map #(str %1 " " %2) key-choices choices)))

