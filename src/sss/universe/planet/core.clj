(ns sss.universe.planet.core
  "Planets - object in universe representing planet"
  (:require [sss.universe.random :as rnd]
            [sss.universe.social.lingvo :as lin]
            [clojure.string :refer [join]]))

(def planet-hard-bounds 
  "Bounds in which planet will be hard"
  [210 710])
(def planet-gas-bounds 
  "Bounds in which planet will be gas (giant)"
  [710 1410])
(def planet-track-bounds 
  "Planet tracks bounds"
  [2 6])
(def planet-climates 
  "Climates, from hot to cold"
  [:very-hot :hot :estimate :cold :very-cold])
(def planet-core 
  "Planet cores depending on planet type"
  {:hard [:iron :nickel :ice] :gas [:rocks]})
(def planet-atmosphere 
  "Planet atmosphere components, from light to heavy"
  [:h2 :he :ar :o2 :n :co2 :kr])
(def planet-name-parts ["no" "lol" "ko" "dna" "kli" "ngo" "ji" "ra"])

(defn gen-size 
  "Generate planet size (* size 1000000) km^2)"
  []
  (if (rnd/chance? 20)
    (apply rnd/r planet-gas-bounds)
    (apply rnd/r planet-hard-bounds)))

(defn gen-type 
  "Choose ~planet type depending on size"
  [planet]
  (if (< (:size planet) 710)
    (assoc planet :type :hard)
    (assoc planet :type :gas)))

(defn gen-core 
  "Generate ~planet core depending on type"
  [planet]
  (rnd/choice (get planet-core (:type planet))))

(defn gen-atmosphere 
  "Generate ~planet atmosphere, depending on size (if bigger that heavier components can be holded)"
  [planet]
  (let [ratio (/ (second planet-hard-bounds) 
                 (- (inc (:size planet)) (first planet-hard-bounds)))
        index (int (* ratio (count planet-atmosphere)))]
    (rnd/choice-num-unique (take index planet-atmosphere) (rnd/r 3 5))))

(defn gen-climate 
  "Generate ~planet climate, depending on track. Closer to star - warmer"
  [planet]
  (let [ratio (/ (- (inc (:track planet)) (first planet-track-bounds)) 
                 (second planet-track-bounds))
        i (int (* (count planet-climates) ratio))]
    (nth planet-climates i)))

(defn gen-name 
  "Generate ~planet name"
  [planet]
  (lin/gen-planet-name))

(defn move-with 
  "Move planet's angle with ~speed"
  [speed]
  (let [start (rnd/r 1 360)]
    (fn [turn]
      (let [moved (double (+ (* turn speed) start))
            years (int (/ moved 360))]
        (- moved (* years 360))))))

(defn gen-planet 
  "Generate planet in ~planets of ~system"
  [planets system]
  (-> {:size (gen-size)
       :track (apply rnd/r planet-track-bounds)
       :pos (move-with 1/512)}
      gen-type
      (#(assoc % :core (gen-core %)))
      (#(assoc % :climate (gen-climate %)))
      (#(assoc % :atmosphere (gen-atmosphere %)))
      (#(assoc % :name (rnd/unique planets :name gen-name %)))
  ))

(defn gen-planets 
  "Generate planets of ~system"
  [system]
  (reduce
    (fn [planets _]
      (conj planets
            (gen-planet planets system)))
    []
    (range (rnd/r 3 6))))

(defn planet-summary 
  "Get ~planet summary info"
  [planet]
  {:name (:name planet)
   :race (:race planet)
   :type (:type planet)
   :core (:core planet)
   :atmosphere (str (:atmosphere planet))
   :climate (:climate planet)
   :size (:size planet)
   :track (:track planet)})

(defn can-live? 
  "Is ~race can live on ~planet?
  Currently all races require :o2, :hard and climate from :cold to :hot"
  [planet race]
  (and
    ((set (:atmosphere planet)) :o2)
    (= :hard (:type planet))
    (#{:estimate :hot :cold} (:climate planet))))
