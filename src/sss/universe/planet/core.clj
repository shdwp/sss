(ns sss.universe.planet.core
  (:require [sss.universe.random :as rnd]
            [clojure.string :refer [join]]))

(def planet-hard-bounds [210 710])
(def planet-gas-bounds [710 1410])
(def planet-track-bounds [2 6])
(def planet-climates [:very-hot :hot :estimate :cold :very-cold])
(def planet-core {:hard [:iron :nickel :ice] :gas [:rocks]})
(def planet-atmosphere [:h2 :he :ar :n :o2 :co2 :kr])
(def planet-name-parts ["no" "lol" "ko" "dna" "kli" "ngo" "ji" "ra"])

(defn gen-size []
  (if (rnd/chance? 20)
    (apply rnd/r planet-gas-bounds)
    (apply rnd/r planet-hard-bounds)))

(defn gen-type [planet]
  (if (< (:size planet) 710)
    (assoc planet :type :hard)
    (assoc planet :type :gas)))

(defn gen-core [planet]
  (rnd/choice (get planet-core (:type planet))))

(defn gen-atmosphere [planet]
  (let [ratio (/ (second planet-hard-bounds) 
                 (- (inc (:size planet)) (first planet-hard-bounds)))
        index (int (* ratio (count planet-atmosphere)))]
    (rnd/choice-num-unique (take index planet-atmosphere) (rnd/r 3 5))))

(defn gen-climate [planet]
  (let [ratio (/ (- (inc (:track planet)) (first planet-track-bounds)) 
                 (second planet-track-bounds))
        i (int (* (count planet-climates) ratio))]
    (nth planet-climates i)))

(defn gen-name [planet]
  (join
    (map
      (fn [_] (rnd/choice planet-name-parts))
      (range (rnd/r 2 3)))))

(defn move-with [speed]
  (let [start (rnd/r 1 360)]
    (fn [tick]
      (let [moved (double (+ (* tick speed) start))
            years (int (/ moved 360))]
        (- moved (* years 360))))))

(defn gen-planet [planets system]
  (-> {:size (gen-size)
       :track (apply rnd/r planet-track-bounds)
       :pos (move-with 1/512)}
      gen-type
      (#(assoc % :core (gen-core %)))
      (#(assoc % :climate (gen-climate %)))
      (#(assoc % :atmosphere (gen-atmosphere %)))
      (#(assoc % :name (rnd/unique planets :name gen-name %)))
  ))

(defn gen-planets [system]
  (reduce
    (fn [planets _]
      (conj planets
            (gen-planet planets system)))
    []
    (range (rnd/r 3 6))))

(defn planet-summary [planet]
  {:name (:name planet)
   :type (:type planet)
   :core (:core planet)
   :atmosphere (str (:atmosphere planet))
   :climate (:climate planet)
   :size (:size planet)
   :track (:track planet)})

