(ns sss.graphics.gui.term
  (:require [sss.graphics.canvas :as can]
            [sss.graphics.core :as gr]
            [sss.graphics.gui.core :as gui]
            [sss.game.gamestate :as gs]
            [clojure.string :refer [join]]))

(defn execute [gs string]
  (try 
    (let [expr (read-string (str string))
          output (str (eval expr))]
      (update-in gs [:meta :term :lines] conj output))
  (catch Exception e (update-in gs [:meta :term :lines] conj (str e)))))

(defn term-ruler [gs _]
  (let [gs (update-in gs [:meta :term :cursor] #(if % % 0))]
    (or
      (cond
        (gs/get-key-in gs [\`]) (update-in gs [:meta :term :active] not)
        (get-in gs [:meta :term :active]) 
        (cond
          (gs/get-key-in gs [:enter]) (-> gs 
                                          (execute (get-in gs [:meta :term :current]))
                                          (update-in
                                            [:meta :term :history]
                                            conj
                                            (get-in gs [:meta :term :current]))
                                          (update-in
                                            [:meta :term :current]
                                            (fn [_] nil)))
          (gs/get-key-in gs [:backspace]) 
          (update-in gs 
                     [:meta :term :current]
                     #(let [cur (get-in gs [:meta :term :cursor])
                            start (join (take cur %))
                            end (join (take-last (- (count %) cur) %))]
                        (str (join (butlast start)) end)))
          (gs/get-key-in gs [:right]) (update-in gs [:meta :term :cursor] inc)
          (gs/get-key-in gs [:left]) (update-in gs [:meta :term :cursor] dec)
          :else (if-let [ch (gs/pop-key gs)]
                  (-> gs
                      (update-in [:meta :term :current]
                                 #(let [cur (get-in gs [:meta :term :cursor] (count %))
                                        start (join (take cur %))
                                        end (join (take-last (- (count %) cur) %))]
                                    (str start ch end)))
                      (update-in [:meta :term :cursor] inc)))))
      gs)))

(defn term-painter [canvas gs]
  (if (get-in gs [:meta :term :active])
    (let [lines (apply 
                  (partial gr/split-str-at 50) 
                  (reverse (get-in gs [:meta :term :lines])))]
      (can/paint 
        (reduce
          #(can/paint %1 (gr/string (second %2)) :t (first %2) :l 0)
          canvas
          (map vector (range) lines))
        (gr/string (get-in gs [:meta :term :current])) 
        :t (count lines) 
        :l 0))
    canvas))
