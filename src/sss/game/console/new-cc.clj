(ns sss.game.console.new-cc
  (:require [sss.game.gamestate :as gs]
            [sss.game.console.core :as console]
            [sss.graphics.canvas :as can]
            [sss.graphics.core :as gr]
            [sss.graphics.gui.group :as g]
            [sss.graphics.gui.component :as cp]))

(defn update [gs -gs]
  (g/update [:ccc :menu] gs -gs))

(defn paint [canvas gs]
  (g/paint canvas gs [:ccc :menu] :t 1 :l 1))

(defn init [gs]
  (g/setup
    gs 
    [:ccc :menu] 
    (g/menu-group-hotkeys
      [:ccc :menu] 
      [\I \G]
      (cp/text (fn [gs]
                 "Gimme gimme"))
      (cp/choice-menu
        ["a" "b"]
        str
        (fn [gs k] gs)
        (fn [gs] gs))
      )))

(defn align
  [gs path]
  (-> gs
      (console/align path)
      (gs/gd-align
        {:name :ccc
         :update update
         :paint paint}
        {:path path
         :mode :info})
      (init)
      ))
