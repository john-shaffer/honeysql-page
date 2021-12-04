#!/usr/bin/env bb

(ns build
  (:require [clojure.edn :as edn]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]))

(def cli-options
  [[nil "--dry-run"]
   ["-h" "--help"]
   ["-v" "--verbose"]])

(defn versions []
  (edn/read-string (slurp "public/versions.edn")))

(defn version-deps [version]
 {:deps {'com.github.seancorfield/honeysql {:mvn/version version}}})

(defn sh! [{:keys [dry-run verbose]} & args]
  (when verbose
    (prn (vec args)))
  (when-not dry-run
    (apply sh/sh args)))

(defmacro return-err
  "Binds the result of shell-call to name-sym and checks its exit code.
  If the exit code is more than zero, print the err output and return
  {:exit exit-code}. Otherwise, execute the body."
  [[name-sym shell-call] & body]
  `(let [result# ~shell-call
         ~name-sym result#
         exit# (:exit result#)]
     (if (and exit# (pos? exit#))
       (do (println (:err result#))
           {:exit exit#})
       (do ~@body))))

(defn compile-version! [options version]
  (sh! options
    "clojure"
    "-Sdeps" (pr-str (version-deps version))
    "-M:shadow-cljs" "release" ":main"))

(defn package-version! [options version]
  (let [dir-name (str "public/v" version)]
    (return-err [_ (sh! options "mkdir" "-p" (str dir-name "/js"))]
      (return-err [_ (sh! options "cp" "public/index.html" dir-name)]
        (sh! options "cp" "public/js/main.js" (str dir-name "/js"))))))

(defn release! [options]
  (loop [[version & more] (versions)]
    (println "Building with HoneySQL" version)
    (return-err [_ (compile-version! options version)]
      (return-err [_ (package-version! options version)]
        (if more
          (recur more)
          {:exit 0})))))

(defn usage [options-summary]
  (->> ["Build the HoneySQL web app"
        ""
        "Usage: build.clj [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  release    Build release version"]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      ;; help => exit OK with usage summary
      (or (:help options) 
        ;; Don't die when loading via cider
        (str/includes? arguments "cider.nrepl/cider-middleware"))
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"release"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (when-not ok? (System/exit 1)))
      (let [{:keys [exit]}
            #__ (case action
                  "release" (release! options))]
        (when (pos? exit) (System/exit exit))))))

(apply -main *command-line-args*)
