(ns sss.universe.social.core
  "Social components of universe.
  Race - single low-level entity group. Currently all of them a humanoids breathing o2."
  (:require [sss.universe.random :as rnd]
            [sss.universe.social.lingvo :as lin]))

(def union-colors [:blue :red :yellow :magenta :green])

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
      (let [race (gen-race races)]
        (assoc races (:name race) race)))
    {}
    (range (rnd/r 3 5))))

(defn races-unions-partition 
  "Parition ~races into unions"
  [races]
  (let [unions-count (rnd/r 2 (dec (count races)))]
    (reduce
      (fn [unions [k race]]
        (let [to (rnd/r (dec (count unions)))]
          (update-in unions [to] conj k)))
      (mapv (fn [[k _]] [k]) (take unions-count races))
      (drop unions-count races))))

(defn gen-races2 [unions]
  (reduce
    (fn [races union]
      
      )
    []
    unions))

(defn gen-union 
  "Gen union in ~unions and using ~races-paritions"
  [unions races-partitions]
  (-> {}
      (assoc :races (set races-partitions))
      (#(assoc % :type (lin/gen-union-type %)))
      (#(assoc % :name (rnd/unique unions :name lin/gen-union-name %)))))

(defn gen-unions 
  "Gen unions of ~races"
  [races]
  (reduce
    (fn [unions [i race-partition]]
      (conj unions (assoc (gen-union unions race-partition) :color (nth union-colors i))))
    []
    (map vector (range) (races-unions-partition races))))

(defn gen-social 
  "Generate social component of universe"
  []
  (let [races (gen-races)
        unions (gen-unions races)]
    {:races races
     :unions unions}))
