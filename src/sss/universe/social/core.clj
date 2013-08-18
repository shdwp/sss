(ns sss.universe.social.core
  (:require [sss.universe.random :as rnd]
            [sss.universe.social.lingvo :as lin]))

(defn gen-race [races]
  (-> {}
      (#(assoc % :name (rnd/unique races :name lin/gen-race-name %)))
      (#(assoc % :lang (lin/gen-race-lang %)))))

(defn gen-races []
  (reduce
    (fn [races _]
      (conj races (gen-race races)))
    []
    (range (rnd/r 3 5))))

(defn fed-partition [races]
  (let [feds-count (rnd/r 2 (dec (count races)))]
    (reduce
      (fn [feds race]
        (let [to (rnd/r (dec (count feds)))]
          (update-in feds [to] conj race)))
      (mapv vector (take feds-count races))
      (drop feds-count races))))

(defn gen-fed [feds race-partition]
  (-> {}
      (assoc :races race-partition)
      (#(assoc % :type (lin/gen-fed-type %)))
      (#(assoc % :name (rnd/unique feds :name lin/gen-fed-name %)))))

(defn gen-federations [races]
  (reduce
    (fn [feds race-partition]
      (conj feds (gen-fed feds race-partition)))
    []
    (fed-partition races)))

(defn gen-social []
  (let [races (gen-races)
        federations (gen-federations races)]
    {:races races
     :feds federations}))
