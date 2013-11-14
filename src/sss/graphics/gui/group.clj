(ns sss.graphics.gui.group
  (:require [sss.game.gamestate :as gs]
            [sss.graphics.gui.component :as cp]
            [sss.graphics.canvas :as can])) 

(defn update [path gs -gs]
   (let [group (apply gs/gd-data gs path)]
     (apply (:update group) gs -gs [])))

(defn paint [canvas gs path & {:keys [t l] :or {t 0 l 0}}]
  (let [group (apply gs/gd-data gs path)
        canv (can/canvas (- (can/w canvas) l) (- (can/h canvas) t) nil)
        canv (apply (:paint group) canv gs [])]
    (can/paint canvas canv :t t :l l)))

(defn set-active [gs path active]
  (gs/gd-set-data gs (conj (vec path) :active) active))

(defn update-active [gs path f]
  (gs/gd-update-data gs (conj (vec path) :active) f))

(defn menu-group [path & comps]
  (let [data (fn [gs p]
               (apply gs/gd-data gs (concat path p)))
        active (fn [gs] (nth comps (data gs [:active])))]
    {:comps comps
     :active 0
     :update (fn [gs -gs]
               (cp/update (active gs) gs -gs))
     :paint (fn [canvas gs]
              (cp/paint (active gs) canvas gs))
     }))

(defn menu-group-hotkeys [path hotkeys & comps]
  (let [data (fn [gs p]
               (apply gs/gd-data gs (concat path p)))
        active (fn [gs] (nth comps (data gs [:active])))]
    {:comps comps
     :active 0
     :update (fn [gs -gs]
               (if-let [k (gs/get-key-in gs hotkeys)]
                 (set-active gs path (.indexOf hotkeys k))
                 (cp/update (active gs) gs -gs)))
     :paint (fn [canvas gs]
              (cp/paint (active gs) canvas gs))
     }) )

(defn setup [gs path group]
  (gs/gd-set-data gs path group))
