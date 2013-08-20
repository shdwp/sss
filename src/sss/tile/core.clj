(ns sss.tile.core
  "Tile - single tile of gmap (tilemap)"
  (:require [sss.graphics.bitmap :as bitmap]))

(defn get-meta 
  "Get metadata of ~tile at ~ks. Provide ~null if you want default value"
  [tile ks & null]
  (get-in (:metadata tile) ks null))

(defn update-meta 
  "Update metadata of ~tile at ~ks with ifn ~f"
  [tile ks f]
  (update-in tile (conj (list* ks) :metadata) f))

(defn set-meta 
  "Set metadata of ~tile at ~ks to ~v"
  [tile ks v]
  (assoc-in tile (conj (list* ks) :metadata) v))

(defprotocol ICharacterable
  (get-character [this]))

(defprotocol IPassability
  (can-pass [this entity]))

(defprotocol IDoorable 
  (toggle [this])
  (toggle [this turn]))

(defprotocol IUsable
  (turn [this gs path x y]))

;; @TODO: figure out why I need extend-protocol instead of simply describe methods here
(defrecord Tile [character passability metadata])

(defn tile [character passability & [metadata]]
  (Tile. character passability metadata))

(defn tile? 
  "Is ~ins instance of Tile?"
  [ins]
  (instance? Tile ins))

;; @TODO: clean up
(defn tiles 
  "Get list of tiles ~ts guaranted"
  [& [t & ts]]
  (concat (if (some #(% t) [vector? list? set? seq?]) t (list t)) (mapcat tiles ts)))

(defn tile-bitmap [t]
  (bitmap/bitmap (tiles t)))

(extend-protocol ICharacterable
  nil (get-character [_] \space)
  String (get-character [this] (.charAt this 0))
  Character (get-character [this] this)
  clojure.lang.IPersistentMap (get-character [this] this)
  Tile (get-character [this] {:fg (or (-> this :metadata :fg) :default) 
                              :bg (or (-> this :metadata :bg) :default)
                              :ch (:character this)}))

(extend-protocol IPassability
  nil (can-pass [_ _] true)
  Character (can-pass [_ _] true)
  String (can-pass [_ _] true)
  Tile (can-pass [this e] ((:passability this) this e)))

(extend-protocol IDoorable
  nil (toggle [_ _])
  Character (toggle [_ _])
  String (toggle [_ _])
  Tile (toggle [this turn]
         (if-let [a (get-meta this [:another])]
           (a turn)
           this)))

(extend-protocol IUsable
  nil (turn [_ gs _ _ _] gs)
  Character (turn [_ gs _ _ _] gs)
  String (turn [_ gs _ _ _] gs)
  Tile (turn [this gs path x y]
         (if (ifn? (get-meta this [:turn]))
           ((get-meta this [:turn]) this gs path x y)
           gs)))

