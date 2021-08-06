(ns com.john-shaffer.honeysql-page
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [com.john-shaffer.honeysql-page.editor :as editor]
            [honey.sql :as sql]
            ["react-highlight" :default Highlight]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(def default-options
  "{:params {:param1 \"gabba\", :param2 2}, :pretty true}")

(def default-query-map
  "{:select-distinct [:f.* :b.baz :c.quux [:b.bla \"bla-bla\"]
                  [[:now]] [[:raw \"@x := 10\"]]]
 :from [[:foo :f] [:baz :b]]
 :join [:draq [:= :f.b :draq.x]
        :eldr [:= :f.e :eldr.t]]
 :left-join [[:clod :c] [:= :f.a :c.d]]
 :right-join [:bock [:= :bock.z :c.e]]
 :where [:or
          [:and [:= :f.a \"bort\"] [:not= :b.baz [:param :param1]]]
          [:and [:< 1 2] [:< 2 3]]
          [:in :f.e [1 [:param :param2] 3]]
          [:between :f.e 10 20]]
 :group-by [:f.a :c.e]
 :having [:< 0 :f.e]
 :order-by [[:b.baz :desc] :c.quux [:f.a :nulls-first]]
 :limit 50
 :offset 10}")

(defn results [{:keys [options query-map]}]
  (let [{[x & more] :result e :error}
        #__ (try
              {:result
               (sql/format (edn/read-string query-map)
                 (edn/read-string options))}
              (catch js/Error e
                {:error e}))]
    [:div {:style {:font-size "16px" :margin-left "24px"}}
     (if e
       (r/create-element Highlight #js{:className "language-clojure"} (pr-str e))
       (r/create-element Highlight #js{:className "language-sql"} x))
     (when more
       (r/create-element Highlight #js{:className "language-clojure"}
         (pr-str (vec more))))]))

(defn App []
  (let [params (try ; Don't die on old browsers
                 (js/URLSearchParams. js/window.location.search)
                 (catch js/Error _))
        get-param #(if params (or (.get params %) %2) %2)
        state (r/atom {:options (get-param "opt" default-options)
                       :query-map (get-param "q" default-query-map)})]
    (fn []
      (let [{:keys [options query-map]} @state]
        (try ; Don't die on old browsers
          (js/window.history.replaceState nil ""
            (if (and (= options default-options)
                  (= query-map default-query-map))
              "."
              (str "?q=" (js/encodeURIComponent query-map)
                "&opt=" (js/encodeURIComponent options))))
          (catch js/Error _))
        [:div
         [:span
          [:a {:href "https://github.com/seancorfield/honeysql"}
           "HoneySQL"]
          " version 2.0.0-rc5 — "
          [:a {:href "https://github.com/john-shaffer/honeysql-page"}
           "GitHub"]
          " — "
          [:a {:href "https://nextjournal.github.io/clojure-mode/#keybindings"}
           "Keybindings"]]
         [:div {:style {:display "flex" :margin-top "10px"}}
          [editor/editor query-map
           {:on-change #(->> % .-state .-doc .toString (swap! state assoc :query-map))}]
          [editor/editor options
           {:on-change #(->> % .-state .-doc .toString (swap! state assoc :options))}]]
         [results {:options options :query-map query-map}]]))))

(defn ^:export ^:dev/after-load init []
  (rdom/render [App]
    (js/document.getElementById "reagent-mount")))
