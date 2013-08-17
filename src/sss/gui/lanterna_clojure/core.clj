(ns sss.gui.lanterna-clojure.core
  (:require [lanterna.screen :as s]
            [sss.graphics.canvas :as can]
            [sss.tile.core :as tile]))

(def ^:dynamic *screen* (atom nil))
(def ^:dynamic *input-thread* (atom nil))

(defn view! [aview ainput]
  (let [last-frame (atom 0)
        fps (atom [0 0 0 0 0 0 0 0 0 0])]
    (reset! *screen* (s/get-screen :swing))
    (->> (fn [] 
           (while true
             (if-let [inp (s/get-key @*screen*)]
               (send ainput #(conj (vec %) inp)))
             (Thread/sleep 10)))
         Thread.
         (reset! *input-thread*)
         .start)
    (s/start @*screen*)
    (while true
      (doall (map-indexed
               (fn [y row]
                 (doall (map-indexed
                          (fn [x ch]
                            (s/put-string @*screen* x y (str (tile/get-character ch))))
                          row)))
               @aview))
      (s/put-string @*screen* 52 0 (str "sss.gui.lanterna-clojure"))
      (s/put-string @*screen* 52 1 (str "fps_" (int (/ (apply + @fps) (count @fps)))))
      (s/put-string @*screen* 52 2 (str "w_" (can/w @aview) " h_" (can/h @aview)))
      (s/redraw @*screen*)
      (swap! fps (fn [o]
                   (butlast
                     (conj o
                           (->> @last-frame 
                                (- (System/currentTimeMillis)) 
                                (/ 1000) 
                                int)))))
      (reset! last-frame (System/currentTimeMillis))
      (Thread/sleep 33))))

(defn shutdown! []
  (s/stop @*screen*)
  (.interrupt @*input-thread*))
