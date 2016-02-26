(ns annotare.views.tagging
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.format :refer [format]]
            [re-frame.core :refer [subscribe dispatch]]
            [annotare.views.common :refer [icon]]
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
        palette (map #(format "hsl(%d, 100%%, 40%%)" %) (range 0 360 hue-step))]
    (into {empty_tag "hsl(180, 0%, 50%)" } (map vector tagset palette))))

(defn get-text-width [text font font-size]
  "Utility function to determine the width of a given string when rendered
   in the browser. Uses an off-screen canvas."
  (let [ctx (.getContext offscreen-canvas "2d")]
    (set! (.-font ctx) (str font-size " " font))
    (.-width (.measureText ctx text))))

(defn tagging-token [token-idx token current-tag tag-set color empty-tag]
  "A single token that is to be tagged"
  (let [extra-space 6
        text-width (get-text-width token "Libre Caslon Display" "28px")
        tag-width (get-text-width current-tag "Helvetica Neue" "10px")
        select-width (+ text-width tag-width extra-space)
        tok-margin (- select-width (/ (- select-width text-width) 2))
        ;; FIXME: We have to work around a bug in Firefox, where the `text-indent`
        ;; value on `select` elements is doubled (http://stackoverflow.com/q/28108434)
        tag-indent (/ (- select-width tag-width)
                      (if (is-firefox?) 4 2))]
    [:div.tag-select
      [:select {:style {:width (str select-width "px")
                        :text-indent (str tag-indent "px")
                        :color color}
                :value current-tag
                :on-change #(do (dispatch [:update-tag token-idx (-> % .-target .-value)]))}
        (for [[idx tag] (indexed tag-set)]
          ^{:key idx} [:option {:value tag}
                       (if (and (= empty-tag current-tag)
                                (= empty-tag tag))
                         " "
                         tag)])]
      [:span.token {:style {:margin-left (str "-" tok-margin "px")
                            :color color}}
       token]]))

(defn tagging-toolbar [project-id tagset-id]
  [:div.tagging-toolbar.columns.is-mobile
   [:div.column.is-2.is-offset-3
    [:a.button
      {:on-click #(dispatch [:next-sentence])
       :title "Skip this sentence"}
     [icon :fast-forward]]]
   [:div.column.is-2
    [:a.button.is-info
     {:on-click #(dispatch [:toggle-modal :tag-help :tagset tagset-id])
      :title "View tag set documentation"}
     [icon :question-circle]]]
   [:div.column.is-2
    [:a.button.is-success
      {:on-click (fn []
                   (dispatch [:submit-sentence]))
       :title "Submit and get new sentence"}
      [icon :check]]]])

(defn tagging-panel []
  (let [sentence (subscribe [:get :active-sentence])
        num-tagged (subscribe [:num-tagged-sentences])
        project (subscribe [:active-project])
        fetching? (subscribe [:get :loading? :initial-sentences])
        submitting? (subscribe [:get :loading? :submit-sentence])
        start-time (subscribe [:get :start-time])]
    (fn []
      (.log js/console @num-tagged)
      (.log js/console @start-time)
      (let [{:keys [tagset id]} @project
            {:keys [tags empty_tag]} tagset
            tag-colors (make-tag-colors tagset)]
        [:section.hero.is-fullheight.tagging-container
         [:div.hero-content
          [:div.container
            (if (or @fetching? @submitting?)
              [:div.loading-spinner]
              [:div
                (if-not @start-time
                  [:h2.subtitle.tagging-hint "Tap on a token to select a tag for it."]
                  (when (> @num-tagged 0)
                    (let [tagging-minutes (/ (- (.getTime (js/Date.)) @start-time) 6e4)]
                      [:p.text-centered.tagging-stats
                       "Tagged " @num-tagged " sentences in " (format "%.1f" tagging-minutes) " minutes"])))
                [:div.tagging-sentence
                  (let [indexed-toks (indexed (map vector (:tokens @sentence) (:tags @sentence)))]
                    (doall (for [[idx [tok tag]] indexed-toks]
                              ^{:key idx}
                              [tagging-token idx tok tag tags (get tag-colors tag) empty_tag])))]])]
          (when (not (or @fetching? @submitting?))
              [tagging-toolbar id (:id tagset)])]]))))
