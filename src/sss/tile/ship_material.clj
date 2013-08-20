(ns sss.tile.ship-material
  "Various ready-to-use tiles for ship building"
  (:require [sss.tile.core :refer :all]))

(defn pass-false [& _] false)
(defn pass-true [& _] true)

(defn metal-rusty-floor [] (tile \. pass-true {:fg :yellow}))
(defn metal-rusty-wall [] (tile \# pass-false {:fg :yellow}))

(defn metal-rusty-door-opened [& [turn]] 
  (tile \_ pass-true {:fg :yellow
                      :another (resolve 'sss.tile.ship-material/metal-rusty-door)
                      :turn turn}))
(defn metal-rusty-door [& [turn]] 
  (tile \x pass-false {:fg :yellow
                       :another metal-rusty-door-opened
                       :turn turn}))

(defn engine [] (tile \E pass-false {:fg :red}))

(defn console-align [n]
    (fn [ch gs path x y]
      (require n)
      (require 'sss.game.console.core)
      (-> gs
          ((resolve 'sss.game.console.core/align) path)
          ((ns-resolve n 'align) path))))

(defn cc-console [] (tile \$ pass-false 
                       {:fg :red
                        :turn (console-align 'sss.game.console.cc) }))
(defn dc-console [] (tile \$ pass-false
                          {:fg :blue
                           :turn (console-align 'sss.game.console.direct-control)}))
(defn bed [] (tile \= pass-true {:fg :red}))
