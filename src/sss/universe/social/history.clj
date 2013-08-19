(ns sss.universe.social.history
  (:require [sss.universe.random :as rnd]
            [sss.universe.planet.core :as planet]))

(defn choose-history-planet [space race]
  (let [system-y (rnd/r 0 (dec (count space)))
        system-x (rnd/r 0 (count (nth space system-y)))
        system (get-in space [system-y system-x])
        planets (filter 
                  second
                  (map-indexed #(vector %1 (planet/can-live? %2 race)) (:planets system)))]
    (if-not (empty? planets)
      [system-y system-x (first (rnd/choice planets))]
      (recur space race))))

(defn setup-history-planet [space history-planet race]
  (update-in space history-planet (fn [p] (assoc p :race race))))

(defn spread [space history-planets]
  (doall (map-indexed
    (fn [y row]
      (doall (map-indexed
        (fn [x system]
          (let [lns (doall (map (fn [[yh xh & pi] [y x sys]]
                           (let [race (get-in space [yh xh :planets pi :race])]
                             (prn yh xh pi)
                           ))
                         history-planets (repeat [y x system])))]
            system
          ))
        row)))
    space)))

(defn process-universe [universe]
  (let [races (-> universe :social :races)
        history-planets (map choose-history-planet (repeat (:space universe)) races)]
    (-> (:space universe)
        (#(reduce
            (fn [space [i race]]
              (let [history-planet (nth history-planets i)]
                (setup-history-planet space history-planet race)))
            %
            (map vector (range) races)))
        (spread history-planets)
  )))

