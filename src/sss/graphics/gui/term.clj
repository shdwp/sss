(ns sss.graphics.gui.term
  (:require [sss.graphics.canvas :as can]
            [sss.graphics.core :as gr]
            [sss.graphics.gui.core :as gui]
            [sss.game.gamestate :as gs]
            [clojure.string :refer [join]]))

(in-ns 'user)
(def gs (atom nil))

(defn mk-path [path]
  (vec (flatten (map #(if (keyword? %) % %) path))))

(defn s [& pv]
  (let [path (mk-path (butlast pv))
        value (last pv)]
    (do (reset! gs (update-in @gs path (fn [_] value)))
        nil)))

(defn g [& path]
  (get-in @gs (mk-path path)))

(defn u [& pi]
  (let [path (mk-path (butlast pi))
        ifn (last pi)]
    (do (reset! gs (update-in @gs path ifn))
        nil)))

(defn actor []
  (cons :universe (:actor-path @gs)))

(in-ns 'sss.graphics.gui.term)

(defn execute [gs string]
  (try 
    (reset! user/gs gs)
    (let [expr (read-string (str string))
          output (str (eval expr))
          gs @user/gs]
      (-> gs
          (update-in [:meta :term :lines] conj output)))
  (catch Exception e (update-in gs [:meta :term :lines] conj (str e)))))

(defn term-ruler [gs _]
  (let [gs (-> gs
               (update-in [:meta :term :cursor] #(if % % 0)))]
    (or
      (cond
        (gs/get-key-in gs [\`]) (update-in gs [:meta :term :active] not)
        (get-in gs [:meta :term :active]) 
        (cond
          (gs/get-key-in gs [:enter]) 
          (-> gs 
              (execute (get-in gs [:meta :term :current]))
              (update-in
                [:meta :term :history]
                conj
                (get-in gs [:meta :term :current]))
              (update-in
                [:meta :term :current]
                (fn [_] nil))
              (update-in [:meta :term :cursor] (fn [_] 0)))
          (gs/get-key-in gs [:backspace]) 
          (-> gs
              (update-in [:meta :term :current]
                         #(let [cur (get-in gs [:meta :term :cursor])
                                start (join (take cur %))
                                end (join (take-last (- (count %) cur) %))]
                            (str (join (butlast start)) end)))
              (update-in [:meta :term :cursor] dec))
          (gs/get-key-in gs [:right]) 
          (update-in gs [:meta :term :cursor] inc)
          (gs/get-key-in gs [:left]) 
          (update-in gs [:meta :term :cursor] dec)
          (gs/get-key-in gs [:up]) 
          (update-in 
            gs 
            [:meta :term :current] 
            (fn [_]
              (first (get-in gs [:meta :term :history]))))
          (gs/get-key-in gs [:down]) 
          (update-in 
            gs 
            [:meta :term :current]
            (fn [_]
              "(s universe space 0 1 ships 0 entities 0 x 0)"))
          :else 
          (if-let [ch (gs/pop-key gs)]
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
      (-> gs :meta :term :lines)
      (can/paint 
        (reduce
          #(can/paint %1 (gr/string (second %2)) :t (first %2) :l 0)
          canvas
          (map vector (range) lines))
        (gr/string (get-in gs [:meta :term :current])) 
        :t (count lines) 
        :l 0))
    canvas))
