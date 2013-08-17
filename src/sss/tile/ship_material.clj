(ns sss.tile.ship-material
  (:require [sss.tile.core :refer :all]))

(defn pass-false [& _] false)
(defn pass-true [& _] true)

(defn metal-rusty-wall [] (tile \# pass-false))
(defn metal-rusty-door-opened [& [tick]] 
  (tile 
    \_
    pass-true 
    {:another 
     (fn [& [t]] (tile \x pass-false {:another metal-rusty-door-opened :tick t}))
     :tick tick}))
(defn metal-rusty-door [& [tick]] (tile \x 
                                pass-false 
                                {:another metal-rusty-door-opened
                                 :tick tick}))

(defn engine [] (tile \E pass-false))

(defn console [] (tile \$ pass-false 
                       {:turn
                        (fn [ch gs path x y]
                          (require 'sss.game.console.cc)
                          (require 'sss.game.console.core)
                          ((resolve 'sss.game.console.core/align) 
                           gs 
                           path 
                           {:update (resolve 'sss.game.console.cc/update)
                            :paint (resolve 'sss.game.console.cc/paint)}))}))
(defn bed [] (tile \B pass-true))
