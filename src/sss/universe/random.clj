(ns sss.universe.random
  )

(defn r 
  "Get random number from ~f to ~t. Someday I'll broke it up and make non random, and will watch"
  ([t] (rand-int t))
  ([f t] (+ (rand-int (inc (- t f))) f)))

(defn chance? 
  "Test you'r luck with chances ~ch"
  [ch]
  (< (r 100) ch))

(defn choice 
  "Randomly choose one of ~choices"
  [choices]
  (nth choices (r (dec (count choices)))))

(defn choice-num
  "Randomly choose ~n items from ~coll"
  [coll n]
  (map
    (fn [_] (choice coll))
    (range n)))

(defn choice-num-unique
  "Randomly choose ~n unique items from ~coll"
  [coll n]
  ((fn [res coll n]
    (if (or (zero? n) (empty? coll))
      res
      (let [ch (choice coll)]
        (recur (conj res ch)
               (filter #(not (= ch %)) coll)
               (dec n))))) [] coll n))

(defn unique 
  "Call ~gen with ~args until it gives unique result depending on contents of ~coll with key ~k"
  ([coll k gen & args]
   (let [n (apply gen args)]
     (if-not (empty? (filter #(= (if (coll? k) (get-in % k) (k %)) n) coll))
       (apply unique coll k gen args)
       n))))
