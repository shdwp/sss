(ns sss.universe.social.politics
  (:require [sss.universe.planet.core :as planet]
            [sss.universe.social.homeland :as hm]))


(defn choose-capitals [universe union]
  (let [capitals (map 
                   #(-> (get (-> universe :social :races) %) :homeland) 
                   (:races union))]
   capitals 
  ))

(defn process-universe [universe]
  (let [capitals (mapcat choose-capitals (repeat universe) (-> universe :social :unions))]
    (update-in 
      universe 
      [:space]
      #(-> %
           ;(setup-capitals capitals)
           ))))

(defn system-owner [universe system]
  (let [race (hm/system-race universe system)] 
    (if (not race)
      :unoccupied
      (some #(if (contains? (:races %) race) % false) (-> universe :social :unions)))))
