(ns sss.universe.station.core
  (:require [sss.universe.random :as rnd]
            [sss.universe.util :as util]))

(def track-bounds 
  "Tracks bounds"
  [4 6])

(defn gen-station 
  [stations system]
  ( -> {:track (apply rnd/r track-bounds)
        :pos (rnd/r 0 360)
        :speed 1/512
        }
       ))

(defn gen-stations 
  [system]
  (reduce (fn [a _] (conj a (gen-station a system)))
          []
          (range (rnd/r 0 1))))
