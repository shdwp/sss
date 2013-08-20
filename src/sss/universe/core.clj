(ns sss.universe.core
  "Universe - universe of a game, containing material and spiritual aspects"
  (:require [clojure.string :refer [split join]]
            [sss.universe.social.core :as social]
            [sss.universe.social.history :as history]
            [sss.universe.space.core :as space]))

(defn gen-universe 
  "Generate universe"
  []
  (-> {:social (time (social/gen-social))
       :space (time (space/gen-space))}
      ;(history/process-universe)

      ))

(defn compare-paths 
  "Compare path (in-universe) ~p1 with ~p2"
  [p1 p2]
  (count (take-while true? (map (fn [p1i p2i] (if (= p1i p2i) true false)) p1 p2))))

(defn unipath 
  "Generate unipath of ~path ~x ~y in ~uni(verse)"
  ([uni path x y] (format "%d;%d.space_sys.%s" x y (:name (get-in uni (take 3 path)))))
  ([s] ;; @TODO
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

