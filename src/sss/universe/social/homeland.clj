(ns sss.universe.social.homeland
  "Generating history of races and applying it to universe."
  (:require [sss.universe.random :as rnd]
            [sss.universe.planet.core :as planet]
            [sss.universe.util :refer :all]))

;; @TODO: perform check if planet is already a homeland of smbd
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

(defn setup-homelands [space races planets]
  (reduce
    (fn [space [k race]]
      (let [history-planet (get planets k)]
        (update-in space history-planet (fn [p] (assoc p :race (:name race))))))
    space
    races))

(defn homeland-distance [ax ay race-name hx hy]
  {(Math/sqrt
     (Math/abs (+ (- hx ax) (- hy ay)))) race-name})

(defn nearest-race-by-homeland [x y homelands]
  (let [lns (mapcat (fn [[n system-path]] (apply homeland-distance 
                                                 x y n 
                                                 (take 2 system-path))) 
                    homelands)]
    (first (vals (apply merge (sorted-map) lns)))))

(defn spread-races [space homelands]
  (mapspace
    (fn [x y system]
      (let [race (nearest-race-by-homeland x y homelands)]
        (-> system
            (update-in
              [:planets]
              #(reduce (fn [planets planet]
                         (conj planets (assoc planet 
                                              :race 
                                              (if (planet/can-live? planet race)
                                                race
                                                :unoccupied))))
                       [] 
                       %)))))
    space))

(defn set-homelands [universe homelands]
  (update-in 
    universe
    [:social :races]
    #(apply 
       merge 
       {} 
       (map
         (fn [[k race]]
           [k (assoc race :homeland (get homelands k))])
         %))))

(defn process-universe [universe]
  (let [races (-> universe :social :races)
        homelands (apply merge {} 
                         (map 
                           (fn [[k race]]
                             [k (choose-history-planet (:space universe) race)])
                           races))]
    (update-in 
      (-> universe
          (set-homelands homelands))
      [:space]
      #(-> %
           (setup-homelands races homelands)
           (spread-races homelands)
           ))))

