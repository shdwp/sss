(defproject sss "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [com.googlecode.lanterna/lanterna "2.1.5"]
                 ;; Used in slick gui backend, currently located in local repository,
                 ;; why its not in normal? its a long story
                 ;[lwjgl/lwjgl "1.0"]
                 ;[lwjgl/lwjgl_util "1.0"]
                 ;[jinput/jinput "1.0"]
                 ;[jnlp/jnlp "1.0"]
                 ;[ibxm/ibxm "1.0"]
                 ;[jorbis/jorbis "1.0"]
                 ;[slick/slick "1.0"]
                 [clojure-lanterna "0.9.4"]
                 [com.taoensso/timbre "2.4.1"]]
  :main sss.core
  
  :offline? false
  :repl-options {:timeout 60000
                 :init (do 
                         (in-ns 'user)
                         (load "autoload") 
                         (autoload/autoload-thread! "src/sss") 
                         (println "Threaded autoloader engaged!")
                         (in-ns 'sss.core))}
  :jvm-opts ["-Djava.library.path=/home/sp/Projects/clojure/sss/"])
