(ns sss.ui.lanterna.core
  (:import java.nio.charset.Charset
           com.googlecode.lanterna.terminal.Terminal
           com.googlecode.lanterna.TerminalFacade
           com.googlecode.lanterna.terminal.text.UnixTerminal
           com.googlecode.lanterna.screen.Screen
           com.googlecode.lanterna.screen.ScreenCharacterStyle
           com.googlecode.lanterna.screen.ScreenWriter)
  (:require [taoensso.timbre.profiling :as profiling :refer [p profile]]
            [sss.ship.core :as ship]
            [sss.gmap.core :as gmap]
            [sss.ui.lanterna.screen :refer :all]
            [sss.graphics.viewport :as view]))

(defn screen []
  (let [term (TerminalFacade/createTerminal)]
    term))

(def ^:dynamic *terminal* (screen))
(def ^:dynamic *input-thread* (atom nil))

(defn read-input [term buff]
  (if-let [i (.readInput term)]
    (recur term (conj buff i))
    buff))

(defn view! [view input-ag]
  (let [term *terminal*
        _ (.enterPrivateMode term)
        buffer (atom [])]
    (->> (fn [] 
          (while true
            (let [inp (read-input term [])]
              (send input-ag concat inp))
            (Thread/sleep 100)))
        Thread.
        (reset! *input-thread*)
        .start)
    (.clearScreen term)
    (while true
      (reset! 
        buffer 
        (refresh
          term 
          @buffer 
          @view))
      (Thread/sleep 300))
    true))

(defn shutdown! []
  (.exitPrivateMode *terminal*)
  (.interrupt @*input-thread*))
