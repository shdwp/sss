(ns sss.game.gamestate
  "Gamestate - big map with all things needed to handle gamecycle iteration. Namespace contains various functions to deal with g(ame)s(tate)"
  (:require [taoensso.timbre :refer [spy]]
            [sss.gmap.core :as gmap]
            [sss.tile.core]))

(def dir-keys (list \h \j \k \l))
(def dirs '(:left :down :up :right))

(defn gamestate 
  "Creates and @return gamestate from ~universe with actor in ~actor-path"
  [universe actor-path & turners]
  {:universe universe
   :actor-path actor-path

   :input {:buffer (agent (repeat 2 nil))
           :blocked false}
   :view (agent nil)
   :tps (repeat 10 0)
   :last-cycle 0

   :gds []
   :turners (conj (or turners []) (resolve 'sss.game.gamestate/log-turn))
   
   :meta {}
   :tick 0
   :turn 0})

(defmacro update 
  "Threaded-like macro, that'll pass ~-gs trough ~forms, but to each form additionaly will be passed static ~-gs. Form function receive 2 args - gs, -gs and generally called ruler
  @return updated ~-gs"
  [-gs & forms]
  (let [forms# (map #(if (list? %)
                       (concat (list (first %)) (list -gs) (rest %))
                       (list % -gs)) forms)]
    `(-> ~-gs ~@forms#)))

(defn actor 
  "Get actor in gamestate ~gs. @return actor (sss.entity.actor)"
  [gs]
  (get-in gs (cons :universe (list* (:actor-path gs)))))

(defn update-actor 
  "Update actor with ~ifn in gamestate ~gs. @return updated ~gs"
  [gs ifn]
  (update-in gs (cons :universe (list* (:actor-path gs))) ifn))

(defn get-universe 
  "get-in for ~gs's universe. @return universe (sss.universe.core)"
  [gs path]
  (get-in gs (cons :universe (list* path))))

(defn update-universe
  "update-in for ~gs's universe. @return updated ~gs"
  [gs path ifn & args]
  (apply (partial update-in gs (cons :universe (list* path)) ifn) args))

;; Game dispatchers

(defn block-line 
  [state]
  (let [unblocking 
        (first (reduce
                 (fn [[line state] ch]
                   (cond 
                     (and (= ch :block)) [(conj line :block) :none]
                     (= ch :unblock) [(conj line :un) :unblock]
                     (= state :unblock) [(conj line :un) state]
                     :else [(conj line ch) state]))
                 [[] :none]
                 state))
        state (reduce
               (fn [[line state] ch]
                 (cond
                   (= ch :un) [(conj line :un) state]
                   (= ch :block) [(conj line false) :block]
                   (= state :block) [(conj line :blo) state]
                   :else [(conj line ch) state]))
               [[] :none]
               (reverse unblocking))]
    (map #(if (= % :blo) true false) (reverse (first state)))))

(defn gds-update 
  "Call :update of each dispatcher in ~-gs. @return updated ~-gs"
  [-gs gs-]
  (-> (reduce
        (fn [gs disp]
          (->
            ((:update disp) 
             gs
             gs-)
            (update-in [:input :blocked] #(if (true? %) % (boolean (:block-input disp))))))
        -gs
        (reverse (:gds gs-)))

      (assoc-in [:input :blocked] false)))

(defn gds-paint 
  "Call :paint of each dispatcher in ~gs with ~canvas-. @return updated ~canvas-"
  [canvas- gs]
  (let [state (block-line 
                (map #(cond 
                        (:block-paint %) :block
                        (:unblock-paint %) :unblock
                        :else false)
                     (:gds gs)))]
    (reduce
      (fn [canvas [i disp]]
        (if (nth state i)
          canvas
          ((:paint disp) canvas gs)))
      canvas-
      (map vector (range) (:gds gs)))))

(defn gd-align 
  "Align ~disp(atcher) to ~gs with ~data.
  ~disp is map, should contain keys:
    :name - name of dispatcher
    :update - update fn [gs -gs]
    :paint - paint fn [canvas gs]
  Additional:
    :block-input - block input to the bottom of game-dispatchers stack
    :block-paint - block paint to the bottom of game-dispatcher stack
    :unblock-paint - unblock paint for current dispatcher and upper to first block
  ~data should be map too.
  @returns updated ~gs"
  [gs disp & [data]]
  (-> gs
      (update-in [:gds] #(conj (vec %) disp))
      (assoc-in [:gds-data (:name disp)] data)))

(defn gd-pop
  "Remove all gd's after one named ~n. @return updated ~gs"
  [gs n]
  (assoc
    gs
    :gds
    (take-while #(not (= (:name %) n)) (:gds gs))))

(defn gd-data-path 
  "Get path of gd named ~n data in ~gs. @return path coll"
  [n & path]
  (concat [:gds-data n] path))

(defn gd-data 
  "Get data in ~path of gd named ~n in ~gs. @return data"
  [gs n & path]
  (get-in gs (apply gd-data-path n path)))

(defn gd-set-data
  "Set gd named ~n data in ~path in ~gs. @return updated ~gs"
  [gs n & pv]
  (assoc-in gs (apply gd-data-path n (butlast pv)) (last pv)))

(defn current-gd 
  "Get last gd in ~gs. @return game dispatcher"
  [gs]
  (last (:gds gs)))

;; Ticks Per Second

(defn limit-tps!
  "Limit tps on ~gs to ~tps by Thread/sleep"
  [gs tps]
  (let [spent (- (System/currentTimeMillis) (:last-cycle gs))
        spent-needed (/ 1000 tps)
        t (int (- spent-needed spent))]
    (if (pos? t)
      (Thread/sleep t))))

(defn tps-ruler 
  "Update ~gs's :last-cycle timestamp. @return updated ~gs"
  [gs _]
  (assoc gs :last-cycle (System/currentTimeMillis)))

(defn tps-counter-ruler 
  "Update ~gs's tps counter (calc. from ~last-cycle). @return updated ~gs"
  [gs _ last-cycle]
  (update-in gs [:tps] (fn [f]
                         (let [spent (- (:last-cycle gs) last-cycle)]
                         (cons
                           (/ 1000 (if (zero? spent) 1 spent))
                           (butlast f))))))

(defn average-tps 
  "Get average tps of ~gs. @return int"
  [gs]
  (int (/ (apply + (:tps gs)) (count (:tps gs)))))

;; Turners - ifn's that'll be called after each turn

(defn add-turner 
  "Add ~turner to ~gs. @return updated ~gs"
  [gs turner]
  (update-in gs [:turners] conj turner))

(defn pop-turner 
  "Pop ~gs's turners list. @return updated ~gs"
  [gs]
  (update-in gs [:turners] #(list* (rest %))))

;; In-game log

(defn log 
  "Push ~msg to ~gs's log at ~turn. @return updated gs"
  [gs turn msg]
  (update-in gs [:log] conj [turn msg]))

(defn log-turn 
  "Turner that cleans the log"
  [gs]
  (update-in gs [:log] (fn [log] (filter #(> 7 (- (:turn gs) (first %))) log))))

;; Input and keys

(defn input-blocked? 
  "Is input in ~gs blocked? @return bool"
  [gs]
  (-> gs :input :blocked))

(defn input 
  "Get input from ~gs. @return input buffer"
  [gs]
  (if-not (input-blocked? gs)
    @(-> gs :input :buffer)
    []))

(defn key-in?
  "Is first key in ~gs in ~ks? @return bool"
  [gs ks]
  (boolean ((set ks) (-> gs input first))))

(defn key-in?
  [gs ks]
  (first (filter #(or (char? %) (keyword? %)) (map #((set ks) %) (-> gs input)))))

(defn pop-key!
  "Pop key from ~gs. @return key"
  [gs]
  (if-not (input-blocked? gs)
    (let [k (first (input gs))]
      (send (-> gs :input :buffer) rest)
      k)
    nil))

(defn remove-key!
  "Remove first occurence of ~k(ey) in ~gs"
  [gs k]
  (let [head (take-while (comp not (partial = k)) (input gs))]
    (send (-> gs :input :buffer) #(concat head [nil] (drop (-> head count inc) %)))
    k))

(defn get-key-in 
  "If first key in ~gs in ~ks, pop it and @return, else @return nil"
  [gs ks]
  (if-let [k (key-in? gs ks)]
    (remove-key! gs k)
    nil))

;; Tick 

(defn turn 
  "Just increment ~gs's turn counter. @return updated ~gs"
  [gs]
  (update-in gs [:turn] inc))

(defn turn? 
  "Is turn in ~gs performed? (compared to 'old' ~-gs). @return bool"
  [gs -gs]
  (> (:turn gs) (:turn -gs)))
            
(defn turn-update 
  "Call turners in ~gs if turn is performed (depending on ~gs and ~gs-)"
  [gs -gs]
  (if (turn? gs -gs)
    (reduce #(%2 %1) gs (:turners gs))
    gs))

;; Direction

(defn direction 
  "Choose a direction depending on a key ~k. @return some of dir-keys"
  [k]
  (if k
    (nth dirs 
         (first (filter (comp not nil?) (map-indexed #(if (= %2 k) %1 nil) dir-keys)))
         nil)
    nil))

(defn xy-apply-dir 
  "Apply direction ~dir to coordinates ~x and ~y, and @return list of (x y)"
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
  [gs entity gmap-path toggle-cond toggle-fn use-cond use-fn]
  `(or 
     (cond
       (apply ~toggle-cond [~gs]) (~toggle-fn ~gs)
       (and (apply ~use-cond [~gs]))
       (if-let [dr# (direction (get-key-in ~gs dir-keys))]
         (let [xy# (xy-apply-dir dr# (:x ~entity) (:y ~entity))
               x# (first xy#)
               y# (second xy#)]
           (if-let [ch# (gmap/char-at (get-in ~gs ~gmap-path) x# y#)]
             (~toggle-fn (apply ~use-fn [~gs ~gmap-path x# y# ch#]))))))
     ~gs))

(defmacro directed-behavior-flag 
  "Directed behavior, but not with toggle-*, but with ~flag-path, ~toggle-key and ~is-turn"
  [gs is-turn entity gmap-path flag-path toggle-key use-fn]
  `(let [use-cond# #(get-in % ~flag-path false)
         toggle-fn# #(update-in % ~flag-path not)
         toggle-cond# #(and (get-key-in % (list ~toggle-key)) (not ~is-turn))]
     (directed-behavior ~gs ~entity ~gmap-path toggle-cond# toggle-fn# use-cond# ~use-fn)))

(defmacro defruler-db-flag 
  "Define directed ruler depends on flag ~flag-path, with ~toggle-key, ~entity-getter and with use-fn constructed with ~use-vars, ~use-forms"
  [n entity-getter flag-path toggle-key use-vars & use-forms]
  `(defn ~n [gs# -gs#]
     (let [use-fn# (fn ~use-vars ~@use-forms)
           entity# (~entity-getter gs#)
           turn# (turn? gs# -gs#)]
       (directed-behavior-flag gs# turn# entity# ~flag-path ~toggle-key use-fn#))))

(defmacro defruler
  "General macro for creating rulers. Creates fn with ~n(ame), and docstring if it's provided (first argument after name)
  Provide :directed argument, and it'll be directed.
  Provide :flag <flag-path>, :key <key>, and it'll be turned with <key> and stated in <flag>
  Last arguments should be - entity-getter, use-vars and use-forms"
  [n & defs]
  (let [doc-string (if (string? (first defs)) (first defs) "")
        defs (if-not (empty? doc-string) (rest defs) defs)
        directed? (some #(= % :directed) defs)
        defs (filter #(not= % :directed) defs)

        defs-map (apply hash-map (if (odd? (count defs)) (butlast defs) defs))
        flag-path (:flag defs-map)
        toggle-key (:key defs-map) 
        defs (if flag-path (filter #(and (not= % :flag) (not= % flag-path)) defs) defs)
        defs (if toggle-key (filter #(and (not= % :key) (not= % toggle-key)) defs) defs)

        entity-getter (first defs)
        use-vars (nth defs 1)
        use-forms (drop 2 defs)]
    `(defn ~n ~doc-string [gs# -gs# gmap-path#]
       (let [use-fn# (fn ~use-vars ~@use-forms)
             entity# (~entity-getter gs#)
             turn# (turn? gs# -gs#)]
         (directed-behavior-flag gs# turn# entity# gmap-path# ~flag-path ~toggle-key use-fn#)))))
