(ns sss.gui.slick.core
  (:import 
    org.newdawn.slick.AppGameContainer
    org.newdawn.slick.BasicGame
    org.newdawn.slick.GameContainer
    org.newdawn.slick.Graphics
    org.newdawn.slick.SlickException)
  (:require [sss.tile.core :refer [get-character]]
            [clojure.string :refer [join]]))

(defn render [g view-ag]
  (doall
    (map-indexed
      #(.drawString g %2 0 (* 15 %1) )
      (map (comp join #(map get-character %)) @view-ag))))

(def ^:dynamic *appgc* (atom nil))

(defn view! [view-ag input-ag]
  (let [game (proxy [BasicGame] ["123"]
               (init [gc])
               (update [gc i])
               (render [gc g] (render g view-ag)))]
    (doto (reset! *appgc* (AppGameContainer. game))
      (.setForceExit false)
      (.start))))

(defn shutdown! []
  (.destroy @*appgc*))
