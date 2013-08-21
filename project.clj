(defproject sss "0.1.0-SNAPSHOT"
  :description "sci-fi roguelike in space written in clojure"
  :url "http://github.com/shadowprince/sss"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [com.googlecode.lanterna/lanterna "2.1.5"]
                 ;; Used in slick gui backend, currently located in local repository,
                 ; why its not in normal? its a long story
;                 [lwjgl/lwjgl "1.0"]
;                 [lwjgl/lwjgl_util "1.0"]
;                 [jinput/jinput "1.0"]
;                 [jnlp/jnlp "1.0"]
;                 [ibxm/ibxm "1.0"]
;                 [jorbis/jorbis "1.0"]
;                 [slick-util "1.0.0"]
                 [clojure-lanterna "0.9.4"]
                 [com.taoensso/timbre "2.4.1"]]
  :main sss.core
  :java-source-paths ["java"]
  
  :repl-options {:timeout 120000
                 :init (do ;; my dev stuff, dont ask about this
                           ;; anyway, I should use Stuart Sierra's reload
                         (in-ns 'user)
                         (load "autoload") 
                         (autoload/autoload-thread! "src/sss") 
                         (println "Threaded autoloader engaged!")
                         (in-ns 'sss.core)
                         (use 'clojure.repl)
                         (use 'clojure.pprint))}
  :jvm-opts ["-Djava.library.path=/home/sp/Projects/clojure/sss/"])
