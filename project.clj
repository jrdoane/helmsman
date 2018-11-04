(defproject io.doane/helmsman "1.1.0-SNAPSHOT"
  :description "This is a library for describing the structure of a web appliction.
               This way, we have uris and data associated directly with routes.
               This is nice for generating navigation or applying middleware to
               routes are a particular level."
  :url "https://github.com/vlacs/helmsman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :pedantic? :warn
  :source-paths ["src"]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/clojure "1.9.0"]
                                  [ring "1.7.1"]
                                  [org.clojure/test.check "0.10.0-alpha3"]]}})
