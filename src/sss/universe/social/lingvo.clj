(ns sss.universe.social.lingvo
  (:require [sss.universe.random :as rnd]
            [clojure.string :refer [join split lower-case]]))

(def race-name-parts ["blah" "ktu" "pa" "lol" "'rva" "klin" "gon"])
(def vowels [\a \e \i \o \u \'])
(def fed-types ["federation", "empire", "conglomerate"])
(def star-name-parts ["osu" "n" "uh" "bo" "ko" "lol" "kso" "uj" "ja" "rr"])
(def text "
It was 1999, and our new online marketing venture was finally off the ground and making a profit using an off-the-shelf conglomeration of bits and pieces of various content management, affiliate program, and ad servers. We'd hit all of the goals for our first funding tranche, and the next step was to use those millions of dollars to grow the staff from 12 to 50, half of which were software developers working directly for me.

The project was an $8 million, nine-month development effort to build, from the ground up, the best 21st-century marketing/e-commerce/community/ad network/reporting system mousetrap possible. Leading a team of 20 people was a big step up, so I buckled down, reading management theory books, re-reading The Mythical Man month, learning the ins and outs of MS Project, Rational Rose, and Requisite Pro, investing in UML and process training, and carefully poring over resumes to find the best candidates.

Having assembled, trained, and indoctrinated my team in current best practice formal software development process, we went to work. We held stakeholder interviews, pored over requirements, developed use case models, charted process flow, designed domain entity models, built our development plan. We developed cleanly separated business logic, persistence, and user experience tiers. We followed formal test-driven development. We held weekly group code reviews. And slowly but surely we carefully moved forward with development.

No, the WTF is not this overly formalized, non-Agile, upfront design, big architecture, strictly controlled waterfall development model. This was what was required to ensured that we suceded, and helped us to hit all of the milestones along the way towards our goal. We of course had our share of back-and-forth with business goals vs. quality vs. scalability vs. time constraints, but overall I have never before or since been on a project which ran as smoothly as this one.") ;; I hope it's not copyrighted

(defn split-word [word]
  ((fn [res [ch & chs]]
     (if-not (nil? ch)
       (recur
         (if-not ((set vowels) ch)
           (conj res 
                 (str (if (empty? res) "^" "")
                      (last (last res)) ch
                      (if (empty? chs) "$" "")))
           (conj (vec (butlast res)) 
                 (str (if (empty? res) "^" "")
                      (last res) ch
                      (if (empty? chs) "$" ""))))
         chs)
       res)
    ) [] word))

(defn chnk [text]
  (flatten 
    (map
      split-word
      (filter #(> (count %) 6)
              (split 
                (reduce
                  (fn [text sub]
                    (lower-case (.replaceAll text sub " ")))
                  text
                  ["\\." "\\," "\\!" "\\?" "\\-" "\\n" "/"])
                #" ")))))

(def chunks (chnk text))
(def start-chunks (filter #(.startsWith % "^") chunks))
(def end-chunks (filter #(.endsWith % "$") chunks))
(def chunks (filter #(not (or (.startsWith % "^") (.endsWith % "$"))) chunks))

(defn gen-word [len]
  (let [len (- len 2)]
    ((fn [word len]
       (let [matching (filter #(.startsWith % (join (take-last 2 word))) chunks)
             chnk (if (empty? matching)
                    (rnd/choice chunks)
                    (join (drop 2 (rnd/choice matching))))
             end-matching (filter #(.startsWith % (join (take-last 2 word))) end-chunks)
             end-chnk (if (empty? end-matching)
                        (rnd/choice end-chunks)
                        (join (drop 2 (rnd/choice end-matching))))]
         (if-not (zero? len)
           (recur (str word chnk) (dec len))
           (str word (join (butlast end-chnk))))
         )) (join (rest (rnd/choice start-chunks))) len)))

(defn gen-race-name [race]
  (gen-word (rnd/r 3 4)))

(defn gen-race-lang [race]
  (join
    (filter
      #(not ((set vowels) %))
      (:name race))))

(defn gen-fed-name [fed]
  (if (= 1 (count (:races fed)))
    (:name (first (:races fed)))
    (gen-word (rnd/r 2 4))))

(defn gen-fed-type [fed]
  (if (= 1 (count (:races fed)))
    (first fed-types)
    (rnd/choice fed-types)))

(defn gen-star-name []
  (gen-word (rnd/r 2 3)))
