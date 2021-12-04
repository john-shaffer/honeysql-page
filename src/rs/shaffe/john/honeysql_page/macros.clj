(ns rs.shaffe.john.honeysql-page.macros
  (:require [clojure.edn :as edn]
            [clojure.java.classpath :as cp]))

(defmacro honeysql-version []
  (->> (cp/classpath)
    (some #(re-matches #"honeysql-(\d+\.\d+\.\d+\.*)\.jar" (.getName %)))
    second))

