(ns sss.ship.autopilot
  (:require [sss.universe.core :as uni]))

(defn enabled? [ship]
  (boolean (get-in ship [:systems :autopilot :enabled])))

(defn destination [ship]
  (-> ship :systems :autopilot :to))

(defn move [dir ship]
  (case dir
    :right (update-in ship [:x] inc)
    :left (update-in ship [:x] dec)
    :up (update-in ship [:y] dec)
    :down (update-in ship [:y] inc)))

(defn move-to [ship dest]
  (let [dx (:x dest)
        dy (dec (:y dest))]
    (cond
      (and (= (:x ship) dx) (= (:y ship) dy)) ship
      (< (:x ship) dx) (move :right ship)
      (> (:x ship) dx) (move :left ship)
      (> (:y ship) dy) (move :up ship)
      (< (:y ship) dy) (move :down ship))))

(defn pilot [ship gs ship-path]
  (let [dest (get-in ship [:systems :autopilot :to])]
    (if (< (uni/compare-paths ship-path dest) 3)
      (move-to ship (:gate (get-in (:universe gs) (drop-last 2 ship-path))))
      ship)))
