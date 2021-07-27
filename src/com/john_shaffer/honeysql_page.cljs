(ns com.john-shaffer.honeysql-page
  (:require [clojure.edn :as edn]
            [honey.sql :as sql]
            [reagent.core :as r]
            [reagent.dom :as rd]))

(def default-options
  "{:params {:param1 \"gabba\", :param2 2}, :pretty true}")

(def default-query-map
  "{:select-distinct [:f.* :b.baz :c.quux [:b.bla \"bla-bla\"]
                  [[:now]] [[:raw \"@x := 10\"]]]
 :from [[:foo :f] [:baz :b]]
 :join [:draq [:= :f.b :draq.x]]
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
  (let [[x & more] (try
                     (sql/format (edn/read-string query-map) (edn/read-string options))
                     (catch js/Error e
                       [e]))]
    [:div {:style {:margin-left "6px"}}
     [:pre x]
     [:pre (when more (pr-str (vec more)))]]))

(defn App []
  (let [state (r/atom {:options default-options
                       :query-map default-query-map})]
    (fn []
      (let [{:keys [options query-map]} @state]
        [:div
         [:span "HoneySQL Version: 2.0.0-rc5"]
         [:div {:style {:display "flex" :margin-top "10px"}}
          [:textarea {:cols 80 :rows 30 :value query-map
                      :on-change #(swap! state assoc :query-map
                                    (.-value (.-target %)))}]
          [:textarea {:cols 80 :rows 5 :value options
                      :on-change #(swap! state assoc :options
                                    (.-value (.-target %)))}]]
         [results {:options options :query-map query-map}]]))))

(defn ^:export ^:dev/after-load init []
  (rd/render [App]
    (js/document.getElementById "reagent-mount")))
