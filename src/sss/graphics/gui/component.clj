(ns sss.graphics.gui.component
  "Components for gui"
  (:require [sss.game.gamestate :as gs]
            [sss.graphics.core :as gr]
            [sss.graphics.canvas :as can]))

(def key-choices (map char (range 97 126)))

(defn blank [a & _]
  a)

(defn paint 
  "Paint ~compo into ~canvas with offset of :t and :l"
  [compo canvas gs & {:keys [t l] :or {t 0 l 0}}]
  (let [canv (can/canvas (- (can/w canvas) l) (- (can/h canvas) t) nil)
        canv (apply (:paint compo) canv gs [])]
    (can/paint canvas canv :t t :l l)))

(defn update 
  "Update ~compo with ~args"
  [compo & args]
  (apply (:update compo) args))

(defn choice-menu 
  "Choice menu component: every of ~entires numbered with characters of range 97..; when one is selected - (~onselect gs k), when canceled (:escape) - (~oncancel gs)"
  [entries string-entry onselect oncancel]
  {:update (fn [gs -gs]
             (or 
               (if (gs/get-key-in gs [:escape])
                 (oncancel gs)
                 (if-let [k (gs/get-key-in gs key-choices)]
                   (let [k (- (int k) 97)]
                     (if (contains? entries k)
                       (onselect gs (nth entries k))))))
               gs))

   :paint (fn [canvas gs]
            (reduce
              (fn [c [i e]]
                (can/paint c (gr/string (str (nth key-choices i) " " (string-entry e))) :t i :l 0))
              canvas
              (map-indexed vector entries)))})

(defn text
  [text-fn]
  {:update blank
   :paint (fn [canvas gs]
            (can/paint canvas (gr/string (text-fn gs)) :t 0 :l 0))})
