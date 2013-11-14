(ns sss.graphics.bitmap
  "Bitmap - 2-level collection, that represents drawable things"
  (:require [clojure.string :refer [split join]]))

(defn bitmap 
  "Make bitmap from ~rows. They can be:
  * collections of: characters (simply), tiles (sss.tile.core.Tile)
  * strings: simply strings, strings with colors (`black:black_text:`default:~black:black_foregrounded_text)"
  [& rows]
  (map (fn [row]
         (if (and (string? row) ((set row) \:))
           (let [parts (split row #"\:")]
             (rest
               (reduce
                 (fn [res part]
                   (cond
                     (.startsWith part "`")
                     (conj (rest res) (assoc (first res) :fg (-> part rest join keyword)))
                     (.startsWith part "~")
                     (conj (rest res) (assoc (first res) :bg (-> part rest join keyword)))
                     :else (concat (vec res)
                                   (map #(assoc (first res) :ch %) part))
                     ))
                 [{}]
                 parts)))
           row))
       rows))

(defn char-at 
  "Character at ~bitmap at ~x and ~y"
  [bitmap x y]
  (let [row (nth bitmap y '())]
    (cond
      (coll? row) (nth row x nil)
      (string? row) (get row x))))

(defn transpose
  "Transpose ~bitmap"
  [[& bitmap]]
  (apply (partial map (fn [& args] (vec args))) bitmap))
