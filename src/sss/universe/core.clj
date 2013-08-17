(ns sss.universe.core
  (:require [clojure.string :refer [split join]]
            [sss.universe.space.core :as space]))

(defn universe []
  {:space (space/space)})

(defn compare-paths [p1 p2]
  (count (take-while true? (map (fn [p1i p2i] (if (= p1i p2i) true false)) p1 p2))))

(defn unipath 
  ([uni path x y] (format "%d;%d.space_sys.%s" x y (:name (get-in uni (take 3 path)))))
  ([s] 
   (let [uni (split s #"_")
         at-system (first uni)
         at-space (second uni)
         space (split at-space #"\.")
         system-name (second space)
         system (split at-system #"\.")
         coords (split (first system) #";")
         planet-name (second system)
         x (Integer/parseInt (first coords))
         y (Integer/parseInt (second coords))]
     )))
