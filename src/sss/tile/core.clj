(ns sss.tile.core
  (:require [sss.graphics.bitmap :as bitmap]))

(defn get-meta [tile ks & null]
  (get-in (:metadata tile) ks null))

(defn update-meta [tile ks f]
  (update-in tile (conj (list* ks) :metadata) f))

(defn set-meta [tile ks v]
  (assoc-in tile (conj (list* ks) :metadata) v))

(defprotocol ICharacterable
  (get-character [this]))

(defprotocol IPassability
  (can-pass [this entity]))

(defprotocol IDoorable 
  (toggle [this])
  (toggle [this tick]))

(defprotocol IUsable
  (turn [this gs path x y]))

(defrecord Tile [character passability metadata]
;  ICharacterable (get-character [this] (:character this))
;  IPassability (can-pass [this e] ((:passability this) this e))
;  IDoorable Tile (toggle [this]
;                   (if (not= nil (get-meta this [:opened]))
;                     (update-meta this [:opened] not)
;                     this)))
  )


(defn tile [character passability & [metadata]]
  (Tile. character passability metadata))

(defn tile? [ins]
  (instance? Tile ins))

(defn tiles [& [t & ts]]
  (concat (if (some #(% t) [vector? list? set? seq?]) t (list t)) (mapcat tiles ts)))

(defn tile-bitmap [t]
  (bitmap/bitmap (tiles t)))

(extend-protocol ICharacterable
  nil (get-character [_] \space)
  String (get-character [this] (.charAt this 0))
  Character (get-character [this] this)
  Tile (get-character [this] (:character this)))

(extend-protocol IPassability
  nil (can-pass [_ _] true)
  Character (can-pass [_ _] true)
  String (can-pass [_ _] true)
  Tile (can-pass [this e] ((:passability this) this e)))

(extend-protocol IDoorable
  nil (toggle [_ _])
  Character (toggle [_ _])
  String (toggle [_ _])
  Tile (toggle [this tick]
         (if-let [a (get-meta this [:another])]
           (a tick)
           this)))

(extend-protocol IUsable
  nil (turn [_ gs _ _ _] gs)
  Character (turn [_ gs _ _ _] gs)
  String (turn [_ gs _ _ _] gs)
  Tile (turn [this gs path x y]
         (if (ifn? (get-meta this [:turn]))
           ((get-meta this [:turn]) this gs path x y)
           gs)))

(defn pprint [bitmap]
  (doall
    (map
      (fn [row]
        (println (reduce
          #(str %1 (get-character %2))
          ""
          row)))
      bitmap)))

