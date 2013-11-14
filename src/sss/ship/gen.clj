(ns sss.ship.gen
  (:require [sss.graphics.bitmap :refer [bitmap]]
            [sss.ship.geom :refer :all]
            [sss.tile.ship-material :as m]
            [sss.tile.core :as tiles]
            [sss.ship.interior :refer :all]))

(let [w (m/metal-rusty-wall)
      e (m/engine)
      b (m/bed)
      f (m/metal-rusty-floor)
      cc (m/cc-console)
      ncc (m/new-cc-console)
      dc (m/dc-console)
      _ tiles/tiles
      blank (_ w (repeat 6 f) w)
      top (repeat 8 w)]

  (defn gen-cc [_] "E")
  (defn gen-engine [_] "E")
  (defn gen-hold [_] "E")
  (defn gen-pass [_] "E")
  (defn gen-living [_] "E")

  (defn gen-cc [ship]
    (bitmap (_ top)
            (_ blank)
            (_ blank)
            (_ w f cc ncc w dc f w)
            (_ blank)
            (_ blank)
            (_ top)))
  
  (defn gen-engine [ship]
    (bitmap (_ top)
            (_ blank)
            (_ w f e e e f f w)
            (_ w f e e e f f w)
            (_ w f e e e f f w)
            (_ blank)
            (_ top)))

  (defn gen-hold [ship]
    (bitmap (_ top)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ top)))

  (defn gen-pass [ship]
    (bitmap (_ top)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ blank)
            (_ top)))

  (defn gen-living [ship]
    (bitmap (_ top)
            (_ blank)
            (_ w f b f f b f w)
            (_ w f b f f b f w)
            (_ w f b f f b f w)
            (_ blank)
            (_ top)))
  
  (defn gen-rusty-door [ship]
    (bitmap (tiles/tiles (m/metal-rusty-door))))
  )


(defn pair-module [ship a b]
  (if (or (nil? a) (nil? b) (= a b))
    ship
    (if-let [side (intersects b a)]
      (make-door ship side b a)
      ship)))

(defn gen-module [ship i]
  (update-in ship [:scheme i] #(if (nil? %) % (assoc % :map (apply (:gen %) ship [])))))

(defn gen-module [ship i]
  ship)

(defn gen-ship [ship]
  (reduce
    (fn [ship imod]
      (gen-module 
        (reduce
          #(pair-module %1 (second imod) %2)
          ship
          (drop (first imod) (:scheme ship))) 
        (first imod)))
    ship
    (map-indexed #(vector %1 %2) (:scheme ship))))
          
