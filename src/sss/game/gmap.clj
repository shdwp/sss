(ns sss.game.gmap
  "Namespace contains various functions that might be helpful in gmap game-dispatcher"
  (:require [sss.game.gamestate :refer [defruler] :as gs]
            [sss.entity.core :as entity]
            [sss.tile.core :as tile]
            [sss.gmap.core :as gmap]
            [taoensso.timbre :refer [spy]]))

(defn skip-ruler 
  "Ruler for skipping turn"
  [gs -gs]
  (if (gs/get-key-in gs [\.])
    (gs/turn gs)
    gs))

(defn actor-ruler 
  "Ruler for actor moving"
  [gs -gs]
  (or (if-not (gs/turn? gs -gs)
        (if-let [k (gs/get-key-in gs [\h \j \k \l])]
          (case k
            \j (gs/turn (gs/update-actor gs #(entity/move % 0 1)))
            \k (gs/turn (gs/update-actor gs #(entity/move % 0 -1)))
            \h (gs/turn (gs/update-actor gs #(entity/move % -1 0)))
            \l (gs/turn (gs/update-actor gs #(entity/move % 1 0))))))
      gs))

(defn actor-collision-ruler 
  "Ruler for actor collisions. If collision detected, turns actor to previous turn state"
  [gs -gs gmap]
  (or
    (when-not (=
             (list (-> (gs/actor gs) :x) (-> (gs/actor gs) :y))
             (list (-> (gs/actor -gs) :x) (-> (gs/actor -gs) :y)))
      (let [ch (gmap/char-at gmap (-> (gs/actor gs) :x) (-> (gs/actor gs) :y))]
        (when-not (tile/can-pass ch (gs/actor gs))
          (gs/update-actor (gs/log gs (:turn gs) "Wall!") (fn [_] (gs/actor -gs))))))
    gs))

(defruler actor-door-ruler 
  "Directed ruler on actor for opening doors"
  :directed :flag [:meta :door-ruler :active] :key \c 
  gs/actor 
  [gs gmap-path x y ch]
  (-> gs
      (update-in gmap-path #(gmap/put-tile % (tile/toggle ch (:turn gs)) x y))
      (gs/turn)))

(defruler actor-use-ruler 
  "Directed ruler on actor to use tile"
  :directed :flag [:meta :use-ruler :active] :key \e
  gs/actor
  [gs yy x y ch]
  (tile/turn ch gs (drop-last 2 (:actor-path gs)) x y))
