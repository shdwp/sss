(ns sss.game.game
  "Root game dispatcher, all gd's aligns to it"
  (:require [sss.ui.lanterna-clojure.core :as ui]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as canvas]
            [sss.graphics.gui.term :as term]
            [sss.gmap.core :as gmap]
            [sss.game.gamestate :as gs]
            [taoensso.timbre :refer [spy]]))

(defn game-cycle 
  [gs-]
  (let [gs (gs/update gs-
                      gs/tps-ruler
                      term/term-ruler
                      (gs/gds-update)
                      (gs/tps-counter-ruler (:last-cycle gs-))
                      gs/turn-update)

        canvas (-> (canvas/canvas 50 40)
                   (gs/gds-paint gs)
                   (term/term-painter gs)
                   (canvas/paint (gr/string (str "turn_" (:turn gs))) :t 0 :r 50)
                   (canvas/paint (gr/string (str "tps_" (gs/average-tps gs))) :t 1 :r 50))]
    (send (:view gs) (fn [_] canvas))
    (gs/limit-tps! gs 20)
    (recur (update-in gs [:tick] inc))))

(def ^:dynamic *gui-thread* (atom nil))

(defn start 
  "Start game on ship with ~gs"
  [gs]
  (->> #(ui/view! (:view gs) (-> gs :input :buffer))
       Thread.
       (reset! *gui-thread*)
       .start)
  (game-cycle gs))

(defn shutdown 
  "Shutdown game (must be called for proper gui threads interruption)"
  []
  (ui/shutdown!)
  (.interrupt @*gui-thread*))
