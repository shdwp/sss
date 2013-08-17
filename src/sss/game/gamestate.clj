(ns sss.game.gamestate
  (:require [taoensso.timbre :refer [spy]]
            [sss.gmap.core :as gmap]
            [sss.tile.core]))

(def dir-keys (list \h \j \k \l))
(def dirs '(:left :down :up :right))

(defn gamestate 
  "Creates gamestate from ~universe with actor in ~actor-path"
  [universe actor-path & turners]
  {:universe universe
   :actor-path actor-path

   :input (agent nil)
   :view (agent nil)

   :turners (if turners 
              (conj turners (resolve 'sss.game.gamestate/log-turn)) 
              [(resolve 'sss.game.gamestate/log-turn)])
   
   :meta {}
   :tick 0})

(defn save!
  "Spit ~gs into output ~output"
  [gs output]
  (spit output (str gs)))

(defn open!
  "Read gs from input ~input"
  [input]
  (read (slurp input)))

(defmacro update 
  "Threaded-like macro, that'll pass ~-gs trough ~forms, but to each form additionaly will be passed static ~-gs. Form function receive 2 args - gs, -gs"
  [-gs & forms]
  (let [forms# (map #(if (list? %)
                       (concat (list (first %)) (list -gs) (rest %))
                       (list % -gs)) forms)]
    `(-> ~-gs ~@forms#)))

(defn actor 
  "Get actor in gamestate ~gs"
  [gs]
  (get-in gs (cons :universe (list* (:actor-path gs)))))

(defn update-actor 
  "Update actor with ~ifn in gamestate ~gs"
  [gs ifn]
  (update-in gs (cons :universe (list* (:actor-path gs))) ifn))

(defn get-universe 
  "get-in for ~gs's universe"
  [gs path]
  (get-in gs (cons :universe (list* path))))

(defn update-universe 
  "update-in for ~gs's universe"
  [gs path ifn]
  (update-in gs (cons :universe (list* path)) ifn))

;; Turners - ifn's that'll be called after each turn

(defn add-turner 
  "Add ~turner to ~gs"
  [gs turner]
  (update-in gs [:turners] conj turner))

(defn pop-turner 
  "Pop ~gs's turners list"
  [gs]
  (update-in gs [:turners] #(list* (rest %))))

;; Quit from gamedisp

(defn quit? 
  "Is quit? setted (with @set-quit)"
  [gs]
  (:quit gs))

(defn set-quit 
  "Set (ask for) quit from current gamedisp"
  [gs]
  (-> gs
      pop-turner
      (assoc :quit true)))

(defn quitted
  "Quit from gamedisp done, clean-up"
  [gs]
  (assoc gs :quit false))

;; In-game log

(defn log 
  "Push ~msg to ~gs's log at ~tick"
  [gs tick msg]
  (update-in gs [:log] conj [tick msg]))

(defn log-turn 
  "Turner that cleans the log"
  [gs]
  (update-in gs [:log] (fn [log] (filter #(> 7 (- (:tick gs) (first %))) log))))

;; Input and keys

(defn input 
  "Get input from ~gs"
  [gs]
  @(:input gs))

(defn key-in?
  "Is first key in ~gs in ~ks?"
  [gs ks]
  (boolean ((set ks) (-> gs input first))))

(defn pop-key 
  "Pop key from ~gs"
  [gs]
  (let [k (first (input gs))]
    (send (:input gs) rest)
    k))

(defn get-key-in 
  "If first key in ~gs in ~ks, pop it and return, else nothing"
  [gs ks]
  (if (key-in? gs ks)
    (pop-key gs)
    nil))

;; Tick 

(defn tick 
  "Just increment ~gs's tick counter"
  [gs]
  (update-in gs [:tick] inc))

(defn tick? 
  "Is tick in ~gs performed? (compared to 'old' ~-gs)"
  [gs -gs]
  (> (:tick gs) (:tick -gs)))
            
(defn tick-update 
  "Call turners in ~gs if tick is performed (depending on ~gs and ~gs-)"
  [gs -gs]
  (if (tick? gs -gs) 
    (reduce #(%2 %1) gs (:turners gs))
    gs))

;; Direction

(defn direction 
  "Choose a direction depending on a key ~k"
  [k]
  (if k
    (nth dirs 
         (first (filter (comp not nil?) (map-indexed #(if (= %2 k) %1 nil) dir-keys)))
         nil)
    nil))

(defn xy-apply-dir 
  "Apply direction ~dir to coordinates ~x and ~y, and return list of (x y)"
  [dir x y & [rev]]
  (let [i (if rev dec inc)
        d (if rev inc dec)]
    (case dir
      :left (list (d x) y)
      :right (list (i x) y)
      :up (list x (d y))
      :down (list x (i y))
      (list x y))))

(defmacro directed-behavior 
  "Directed behavior macro.
  If (~toggle-cond ~gs), than call (~toggle-fn ~gs),
  If (~use-cond ~gs), than call (~toggle-fn (~use-fn ~gs x y char-at-x-y))"
  [gs entity toggle-cond toggle-fn use-cond use-fn]
  `(or 
     (cond
       (apply ~toggle-cond [~gs]) (~toggle-fn ~gs)
       (and (apply ~use-cond [~gs]))
       (if-let [dr# (direction (get-key-in ~gs dir-keys))]
         (let [xy# (xy-apply-dir dr# (:x ~entity) (:y ~entity))
               x# (first xy#)
               y# (second xy#)]
           (if-let [ch# (gmap/char-at  (:ship ~gs) x# y#)]
             (~toggle-fn (apply ~use-fn [~gs x# y# ch#]))))))
     ~gs))

(defmacro directed-behavior-flag 
  "Directed behavior, but not with toggle-*, but with ~flag-path, ~toggle-key and ~is-tick"
  [gs is-tick entity flag-path toggle-key use-fn]
  `(let [use-cond# #(get-in % ~flag-path false)
         toggle-fn# #(update-in % ~flag-path not)
         toggle-cond# #(and (get-key-in % (list ~toggle-key)) (not ~is-tick))]
     (directed-behavior ~gs ~entity toggle-cond# toggle-fn# use-cond# ~use-fn)))

(defmacro defruler-db-flag 
  "Define directed ruler depends on flag ~flag-path, with ~toggle-key, ~entity-getter and with use-fn constructed with ~use-vars, ~use-forms"
  [n entity-getter flag-path toggle-key use-vars & use-forms]
  `(defn ~n [gs# -gs#]
     (let [use-fn# (fn ~use-vars ~@use-forms)
           entity# (~entity-getter gs#)
           tick# (tick? gs# -gs#)]
       (directed-behavior-flag gs# tick# entity# ~flag-path ~toggle-key use-fn#))))

(defmacro defruler
  "General macro for creating rulers.
  Provide :directed argument, and it'll be directed.
  Provide :flag <flag-path>, :key <key>, and it'll be turned with <key> and stated in <flag>
  Last arguments should be - entity-getter, use-vars and use-forms"
  [n & defs]
  (let [directed? (some #(= % :directed) defs)
        defs (filter #(not= % :directed) defs)

        defs-map (apply hash-map (if (odd? (count defs)) (butlast defs) defs))
        flag-path (:flag defs-map)
        toggle-key (:key defs-map) 
        defs (if flag-path (filter #(and (not= % :flag) (not= % flag-path)) defs) defs)
        defs (if toggle-key (filter #(and (not= % :key) (not= % toggle-key)) defs) defs)

        entity-getter (first defs)
        use-vars (second defs)
        use-forms (drop 2 defs)
        ]
    `(defn ~n [gs# -gs#]
       (let [use-fn# (fn ~use-vars ~@use-forms)
             entity# (~entity-getter gs#)
             tick# (tick? gs# -gs#)]
         (directed-behavior-flag gs# tick# entity# ~flag-path ~toggle-key use-fn#)))))
