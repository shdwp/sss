(ns sss.universe.social.core
  "Social components of universe.
  Race - single low-level entity group. Currently all of them a humanoids breathing o2."
  (:require [sss.universe.random :as rnd]
            [sss.universe.social.lingvo :as lin]))

(defn gen-race 
  "Generate race in ~races"
  [races]
  (-> {}
      (#(assoc % :name (rnd/unique races :name lin/gen-race-name %)))
      (#(assoc % :lang (lin/gen-race-lang %)))))

(defn gen-races 
  "Generate races"
  []
  (reduce
    (fn [races _]
      (conj races (gen-race races)))
    []
    (range (rnd/r 3 5))))

(defn races-unions-partition 
  "Parition ~races into unions"
  [races]
  (let [unions-count (rnd/r 2 (dec (count races)))]
    (reduce
      (fn [unions race]
        (let [to (rnd/r (dec (count unions)))]
          (update-in unions [to] conj race)))
      (mapv vector (take unions-count races))
      (drop unions-count races))))

(defn gen-union 
  "Gen union in ~unions and using ~races-paritions"
  [unions races-partitions]
  (-> {}
      (assoc :races races-partitions)
      (#(assoc % :type (lin/gen-union-type %)))
      (#(assoc % :name (rnd/unique unions :name lin/gen-union-name %)))))

(defn gen-unions 
  "Gen unions of ~races"
  [races]
  (reduce
    (fn [unions race-partition]
      (conj unions (gen-union unions race-partition)))
    []
    (races-unions-partition races)))

(defn gen-social 
  "Generate social component of universe"
  []
  (let [races (gen-races)
        unions (gen-unions races)]
    {:races races
     :unions unions}))
