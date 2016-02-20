(ns annotare.views.tagging
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :refer [subscribe dispatch]]
            [annotare.util :refer [indexed]]))

;; Offscreen-Canvas for determining text-width
(def offscreen-canvas (.createElement js/document "canvas"))

(defn get-text-width [text font font-size]
  "Utility function to determine the width of a given string when rendered
   in the browser. Uses an off-screen canvas."
  (let [ctx (.getContext offscreen-canvas "2d")]
    (set! (.-font ctx) (str font-size " " font))
    (.-width (.measureText ctx text))))

(defn tagging-token [token-idx token current-tag tag-set]
  "A single token that is to be tagged"
  (let [text-width (get-text-width token "Helvetica Neue" "56px")
        tag-width (get-text-width (apply (partial max-key count) tag-set) "Helvetica Neue" "14px")
        select-width (+ text-width tag-width)]
    [:div.tag-select {:style {:margin-right (str (* 1.25 tag-width) "px")}}
      [:select {:style {:width (str select-width "px")
                        :padding-left (str text-width "px")}
                :value current-tag
                :on-change #(dispatch [:update-tag token-idx (-> % .-target .-value)])}
        (for [[idx tag] (indexed tag-set)]
          ^{:key idx} [:option {:value tag} tag])]
      [:span.token {:style {:margin-left (str "-" select-width "px")}} token]]))

(defn tagging-toolbar [project-id]
  [:section>div.tagging-toolbar
   [:button.button.is-primary
    {:on-click #(dispatch [:submit-sentence])}
    "Done"]
   [:button.button
    {:on-click #(dispatch [:fetch-random-sentence])}
    "Skip"]])

(defn tagging-panel []
  (let [sentence (subscribe [:active-sentence])
        project (subscribe [:active-project])]
    (fn []
      (let [{:keys [tagset id]} @project]
        [:div.container
         ;[:pre (with-out-str (pprint @sentence))]
         [tagging-toolbar id]
         (doall (for [[idx [tok tag]] (indexed (map vector (:tokens @sentence) (:tags @sentence)))]
                   ^{:key idx} [tagging-token idx tok tag tagset]))]))))

