(ns autoload
  (:import java.io.File)
  (:use [clojure.string :only (split)]))

(defn get-files-mdates [d]
  (let [entries (.listFiles d)
        dirs (filter #(.isDirectory %) entries)
        files (reduce 
                #(assoc %1 (.getAbsolutePath %2) (.lastModified %2)) 
                {} 
                (->> entries
                  (filter #(.isFile %))
                  (filter #(-> % (.getName) (.endsWith "clj")))))]
    (merge files (mapcat get-files-mdates dirs))))

(defn autoload!
  ([path] (autoload! path false []))
  ([path verbose] (autoload! path verbose []))
  ([path verbose coll]
   (let [mdates (get-files-mdates (File. path))]
     (doseq [f mdates]
       (let [k (key f)
             oldts (get coll k)
             newts (val f)]
         (if (not= oldts newts)
           (do
             (if verbose
               (print (str "File " k " modified, loading... ")))
             (load-file k)
             (if verbose
               (println "ok."))))))
     mdates)))

(def update-omdates (atom {}))

(defn autoload-update! [path]
  (reset! update-omdates (autoload! path true @update-omdates)))

(defn autoload-exception-handle! [path e]
  (let [mdates (get-files-mdates (File. path))]
    (.printStackTrace e)
    mdates))

(defn autoload-thread-fn [path verbose]
  (let [omdates (atom {})]
    (fn [] 
      (while true
        (try
          (reset! omdates (autoload! path verbose @omdates))
          (catch Exception e (reset! omdates (autoload-exception-handle! path e))))
        (Thread/sleep 500)))))

(def thread-instance (atom nil))

(defn autoload-thread!
  ([path] (autoload-thread! path false))
  ([path verbose] (autoload-thread! path verbose thread-instance))
  ([path verbose & [thread-atom]]
   (if (and (instance? Thread @thread-atom) (.running @thread-atom))
     (prn "Thread " @thread-atom " in atom " thread-atom " is already running!")
     (.start (reset! thread-atom (Thread. (autoload-thread-fn path verbose)))))))


(defn autoload-thread-interrupt! 
  ([] (autoload-thread-interrupt! thread-instance))
  ([thread-atom] (.interrupt @thread-atom)))


;; 
;; PROJECT ZONE
;; 


