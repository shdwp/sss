(ns sss.game.core
  "Top level game-dispatcher.
  Game-dispatcher is bunch of functions to deal current level of game."
  (:require [sss.game.gamestate :as gs]
            [sss.game.ship.core :as game-ship]
            [sss.game.game :as game-game] ;; @TODO
            [sss.universe.core :as uni]
            [sss.ship.core :as ship]
            [sss.ship.gen :as ship-gen]
            [sss.entity.actor :as actor]
            [sss.ship.autopilot :as autopi]))

(defn autopilot-system 
  "System for autopilotting ships in ~system-path of ~gs's universe"
  [gs system-path]
  (gs/update-universe
    gs 
    system-path
    (fn [system]
      (update-in system 
                 [:ships] 
                 (fn [ships]
                   (reduce #(conj 
                              %1 
                              (autopi/pilot 
                                %2 
                                gs 
                                (concat system-path [:ship (count %1)]))) [] ships))))))

(defn gate-system 
  "System for moving ships take from gate queue to other systems from ~system-path in ~gs's universe. You must provide ~actor-path to change it if actor's ship was moved."
  [gs system-path actor-path]
  (if-let [task (first (gs/get-universe gs (concat system-path [:gate :queue])))]
    (let [ship-path (first task)
          ship (gs/get-universe gs ship-path)
          dest (second task)
          new-ship-path (concat 
                          [:space]
                          dest 
                          [:ships (count (gs/get-universe gs (concat dest [:ships])))])]
      ;; @TODO: check if ship is near gate, if else - put it to the last
      (-> (if (< (uni/compare-paths actor-path ship) 4)
            (update-in gs [:actor-path] (fn [_] 
                                        (concat
                                          new-ship-path 
                                          (take-last 2 (:actor-path gs)))))
            gs)
          (gs/update-universe ship-path (fn [_] nil))
          (gs/update-universe new-ship-path (fn [_] ship))
          (gs/update-universe (concat system-path [:gate :queue]) rest))
      )
    gs)
  )

(defn universe-turner 
  "Turner for entire universe!"
  [gs]
  (let [system-path (take 3 (:actor-path gs))]
    (-> gs
        (autopilot-system system-path)
        (gate-system system-path (:actor-path gs)))))

(defn gen-gs 
  "Generate gamestate"
  []
  (let [universe (-> (uni/gen-universe)
                     (update-in [:space 0 1 :ships 0]
                                (fn [& _] (ship-gen/gen-ship (ship/-ship))))
                     (update-in [:space 0 1 :ships 0 :entities 0] 
                                (fn [& _] (actor/actor 9 2))))
          actor-path [:space 0 1 :ships 0 :entities 0]]
        (gs/gamestate universe actor-path universe-turner)))

(defn start 
  "Start game with ~gs (or without)"
  [& [gs]]
  (game-game/start (game-ship/align (time (gen-gs)))))

