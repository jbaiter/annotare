(ns annotare.views.tagging
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.string.format]
            [re-frame.core :refer [subscribe dispatch]]
            [annotare.util :refer [indexed]]))

;; Offscreen-Canvas for determining text-width
(def offscreen-canvas (.createElement js/document "canvas"))

(defn is-firefox? []
  "Check if we're running on Firefox"
  (exists? js/InstallTrigger))

(defn make-tag-colors [{:keys [empty_tag tags]}]
  (let [tagset (disj tags empty_tag)
        num-tags (count tagset)
        hue-step (/ 360 num-tags)
        palette (map #(goog.string/format "hsl(%d, 100%%, 40%%)" %) (range 0 360 hue-step))]
    (into {empty_tag "hsl(180, 0%, 50%)" } (map vector tagset palette))))

(defn get-text-width [text font font-size]
  "Utility function to determine the width of a given string when rendered
   in the browser. Uses an off-screen canvas."
  (let [ctx (.getContext offscreen-canvas "2d")]
    (set! (.-font ctx) (str font-size " " font))
    (.-width (.measureText ctx text))))

(defn tagging-token [token-idx token current-tag tag-set color]
  "A single token that is to be tagged"
  (let [extra-space 6
        text-width (get-text-width token "Helvetica Neue" "28px")
        max-tag-width (get-text-width (apply (partial max-key count) tag-set) "Helvetica Neue" "10px")
        tag-width (get-text-width current-tag "Helvetica Neue" "10px")
        select-width (+ text-width tag-width extra-space)
        tok-margin (- select-width (/ (- select-width text-width) 2))
        ;; FIXME: We have to work around a bug in Firefox, where the `text-indent`
        ;; value on `select` elements is doubled (http://stackoverflow.com/q/28108434)
        tag-indent (/ (- select-width tag-width)
                      (if (is-firefox?) 4 2))]
    [:div.tag-select {:style {:margin-right (str max-tag-width "px")}}
      [:select {:style {:width (str select-width "px")
                        :text-indent (str tag-indent "px")
                        :color color}
                :value current-tag
                :on-change #(dispatch [:update-tag token-idx (-> % .-target .-value)])}
        (for [[idx tag] (indexed tag-set)]
          ^{:key idx} [:option {:value tag} tag])]
      [:span.token {:style {:margin-left (str "-" tok-margin "px")
                            :color color}}
       token]]))

(defn tagging-toolbar [project-id]
  [:div.tagging-toolbar.columns
   [:div.column.is-2.is-offset-3
    [:button.button
      {:on-click #(dispatch [:next-sentence])}
      "Skip"]]
   [:div.column.is-2.is-offset-2
    [:button.button.is-success
      {:on-click (fn []
                   (.scroll js/window 0 0)
                   (dispatch [:submit-sentence]))}
      "Done"]]])

(defn tagging-panel []
  (let [sentence (subscribe [:active-sentence])
        project (subscribe [:active-project])]
    (fn []
      (let [{:keys [tagset empty_tag id]} @project
            tag-colors (make-tag-colors tagset)]
        [:section.hero.is-medium
         [:div.hero-content>div.container
          (doall (for [[idx [tok tag]] (indexed (map vector (:tokens @sentence) (:tags @sentence)))]
                    ^{:key idx} [tagging-token idx tok tag (:tags tagset) (get tag-colors tag)]))
          [tagging-toolbar id]]]))))

