(defproject hourglass "0.1.0-SNAPSHOT"
  :description "An opinionated wrapper for the Java 8 Date-Time API."
  :url "https://github.com/hden/hourglass"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [inflections "0.14.0"]]
  :plugins [[lein-cloverage "1.2.4"]]
  :repl-options {:init-ns hourglass.core})
