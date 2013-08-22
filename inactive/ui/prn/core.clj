(ns sss.ui.prn.core
  (:require [sss.tile.core :as tile]))

(defn view! [view-ag _]
  (while true
    (doall (map-indexed
             (fn [y row]
               (doall (map-indexed
                        (fn [x ch]
                          (print (tile/get-character ch)))
                        row))
               (println))
             @view-ag))
    (Thread/sleep 1000)))

(defn shutdown! []
  )
