(ns sss.gui.lwjgl.core
  (:import org.sp.sss.LWJGLBackend
           org.lwjgl.opengl.Display
           org.lwjgl.input.Keyboard
           org.newdawn.slick.Color)

  (:require [sss.tile.core :as tile]))

(def ^:dynamic *backend* (atom nil))

(defn input [ainput]
  (while (Keyboard/next)
    (let [k (Keyboard/getEventCharacter)]
      (if-not (zero? (int k))
        (send ainput #(conj (vec %) k))))))

(defn render [this aview]
  (input nil)
  (.bind (Color/white))
  (.draw this (str (.currentFps this)) 10 10)
  (doall 
    (map-indexed
      (fn [y row]
        (.draw 
          this (reduce
                 (fn [s cell]
                   (let [ch (tile/get-character cell)
                         fg (if (map? ch) (:fg ch) :default)
                         bg (if (map? ch) (:bg ch) :default)
                         ch (if (map? ch) (:ch ch) ch)]
                     (str s ch)))
                 ""
                 row)
          0
          (* 20 row)))
      @aview)))

(defn view! [aview ainput]
  (let [back (reset! *backend* (proxy [LWJGLBackend] [] 
                                 (render [] (input ainput) (render this aview))))]
    (.start back 500 500 60)))

(defn shutdown! []
  (.stop @*backend*))
