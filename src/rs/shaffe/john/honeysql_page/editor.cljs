(ns rs.shaffe.john.honeysql-page.editor
  (:require ["@codemirror/commands" :refer [history historyKeymap]]
            ["@codemirror/language" :refer [foldGutter syntaxHighlighting defaultHighlightStyle]]
            ["@codemirror/state" :refer [EditorState]]
            ["@codemirror/view" :as view :refer [EditorView ViewUpdate]]
            [applied-science.js-interop :as j]
            [nextjournal.clojure-mode :as cm-clj]
            [nextjournal.clojure-mode.live-grammar :as live-grammar]
            [reagent.core :as r]))

(def theme
  (.theme EditorView
          (j/lit {".cm-content" {:white-space "pre-wrap"
                                 :padding "10px 0"}
                  "&.cm-focused" {:outline "none"}
                  ".cm-line" {:padding "0 9px"
                              :line-height "1.6"
                              :font-size "16px"
                              :font-family "var(--code-font)"}
                  ".cm-matchingBracket" {:border-bottom "1px solid var(--teal-color)"
                                         :color "inherit"}
                  ".cm-gutters" {:background "transparent"
                                 :border "none"}
                  ".cm-gutterElement" {:margin-left "5px"}
                  ;; only show cursor when focused
                  ".cm-cursor" {:visibility "hidden"}
                  "&.cm-focused .cm-cursor" {:visibility "visible"}})))

(defonce extensions #js[theme
                        (history)
                        (syntaxHighlighting defaultHighlightStyle)
                        (view/drawSelection)
                        (foldGutter)
                        (.. EditorState -allowMultipleSelections (of true))
                        (if false
                          ;; use live-reloading grammar
                          #js[(cm-clj/syntax live-grammar/parser)
                              (.slice cm-clj/default-extensions 1)]
                          cm-clj/default-extensions)
                        (.of view/keymap cm-clj/complete-keymap)
                        (.of view/keymap historyKeymap)])

(defn make-state [extensions doc]
  (.create EditorState
    #js{:doc doc
        :extensions (cond-> #js[(.. EditorState -allowMultipleSelections (of true))]
                      extensions
                      (j/push! extensions))}))

(defn editor [source {:keys [on-change]}]
  (r/with-let [update-listener (fn [^ViewUpdate view-update]
                                 (when (and (.-docChanged view-update) on-change)
                                   (on-change view-update)))
               update-extension (-> EditorView .-updateListener (.of update-listener))
               !view (r/atom nil)
               mount! (fn [el]
                        (when el
                          (reset! !view (new EditorView
                                             (j/obj :state
                                                    (make-state
                                                      (-> #js [extensions]
                                                       (.concat #js [update-extension]))
                                                     source)
                                                    :parent el)))))]
    [:div
     [:div {:class "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
            :ref mount!
            :style {:max-height 410}}]]
    (finally
      (j/call @!view :destroy))))
