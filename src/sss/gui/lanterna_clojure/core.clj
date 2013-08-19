(ns sss.gui.lanterna-clojure.core
  "GUI using lanterna-clojure library, currenly used (but not primary becose low perfomance)"
  (:require [lanterna.screen :as s]
            [clojure.string :refer [join]]
            [sss.graphics.canvas :as can]
            [sss.tile.core :as tile]))

(def ^:dynamic *screen* (atom nil))
(def ^:dynamic *input-thread* (atom nil))

(defn limit-fps [last-frame fps]
  (let [spent (- (System/currentTimeMillis) last-frame)
        spent-needed (/ 1000 fps)
        sleep (- spent-needed spent)]
    (if (pos? sleep) (Thread/sleep sleep))))

(defn view! 
  "View agent ~aview, collect input into ~ainput. Blocks current thread, so should be called in separate thread."
  [aview ainput]
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
      (reset! last-frame (System/currentTimeMillis))
      (s/clear @*screen*)
      (doall (map-indexed
               (fn [y row]
                 (doall (map-indexed
                          (fn [x ch]
                            (let [ch (tile/get-character ch)
                                  fg (if (map? ch) (:fg ch) :default)
                                  bg (if (map? ch) (:bg ch) :default)
                                  c (if (map? ch) (:ch ch) ch)]
                              (s/put-string @*screen* x y (str c) {:fg fg :bg bg})))
                          row)))
               @aview))
      (s/put-string @*screen* 50 0 (str "|fps_" (int (/ (apply + @fps) (count @fps)))))
      (s/put-string @*screen* 50 1 (str "|w_" (can/w @aview) " h_" (can/h @aview)))
      (s/put-string @*screen* 50 2 (join (repeat 20 "-")))
      (s/redraw @*screen*)
      
      (limit-fps @last-frame 60)
      (swap! fps (fn [o]
                   (cons
                     (/ 1000 (- (System/currentTimeMillis) @last-frame))
                     (butlast o)))))))

(defn shutdown! 
  "Shutdown gui"
  []
  (s/stop @*screen*)
  (.interrupt @*input-thread*))
