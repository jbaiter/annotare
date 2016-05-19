(ns annotare.views.tagging
  (:require-macros [annotare.macros :refer [log]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.format :refer [format]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [annotare.views.common :refer [icon]]
            [annotare.util.core :refer [ev->val]]
            [annotare.util.common :refer [indexed pair-seq extract-entities
                                          has-entities?]]))

;; Offscreen-Canvas for determining text-width
(def offscreen-canvas (.createElement js/document "canvas"))

(defn is-firefox? []
  "Check if we're running on Firefox"
  (exists? js/InstallTrigger))

(defn strip-tag [tag]
  (if (nil? tag)
    tag
    (string/replace tag #"^B-", "")))

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

(defn tagging-token [token-idx token current-tag tag-set color empty-tag prev-tag]
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
                         tag)])
        (if (and (not (nil? prev-tag)) (not= prev-tag empty-tag))
          ^{:key :begin} [:option (str "B-" prev-tag)])]
      [:span.token {:style {:margin-left (str "-" tok-margin "px")
                            :color color}}
       token]]))

(defn tagging-toolbar [project-id has-prev?]
  [:div.tagging-toolbar
    (when has-prev?
      [:a.button.is-large.prev-btn
       {:on-click #(dispatch [:previous-sentence])
        :title "Go to previous sentence"}
       [icon :fast-backward]])
    [:a.button.is-large.skip-btn
      {:on-click #(dispatch [:next-sentence])
       :title "Skip this sentence"}
     [icon :fast-forward]]
    [:a.button..is-large.next-btn
      {:on-click (fn []
                   (dispatch [:submit-sentence])
                   (dispatch [:next-sentence]))
       :title "Submit and get new sentence"}
      [icon :check]]])

(defn tagging-sentence [tokens tags tag-colors empty_tag]
  [:div.tagging-sentence
    (let [indexed-toks (indexed (pair-seq (map vector tokens tags) 2))
          tagset (-> tag-colors keys sort)]
      (doall (for [[idx [[_ prev-tag] [tok tag]]] indexed-toks]
                ^{:key idx}
                [tagging-token idx tok tag tagset
                               (get tag-colors (strip-tag tag)) empty_tag
                               prev-tag])))])

(defn tagging-info []
  (let [num-tagged (subscribe [:num-tagged-sentences])
        start-time (subscribe [:get :start-time])]
    (fn []
      (if-not @start-time
        [:h2.subtitle.tagging-hint "Tap on a token to select a tag for it."]
        (when (> @num-tagged 0)
          (let [tagging-minutes (/ (- (.getTime (js/Date.)) @start-time) 6e4)]
            [:p.text-centered.tagging-stats
              "Tagged " @num-tagged " sentences in "
              (format "%.1f" tagging-minutes) " minutes"]))))))

(defn entity-info [info]
  [:div.card>div.card-content
   [:div.media>div.media-content
    [:p.title.is-5 (:name info)]
    [:p.subtitle.is-6 (:lifespan info)]]
   [:div.content
    (:info info)
    [:br]
    [:small.a {:href (:url info)}]]])

(defn linking-suggestions [query-key]
  (let [suggestions (subscribe [:get :gnd-queries query-key])]
    (fn [query-key]
      ;; TODO: On click, link the entity to the tagged sequence
      [:div.linking-suggestions
        (for [[idx info] (indexed @suggestions)]
          ^{:key idx} [entity-info info])])))

(defn linking-widget [{:keys [tokens tag] :as ent}
                      query-key loading? show-suggestions?]
  (let [query (subscribe [:get :linking-inputs query-key])]
    (fn [{:keys [tokens tag] :as ent}
         query-key loading? show-suggestions?]
      (let [name (string/join " " tokens)]
        [:div.linking-widget
          [:div.column.card
           {:on-click #(dispatch [:set [:show-suggestions query-key]
                                       (not show-suggestions?)])}
           [:div.card-content
            [:div.media
              [:div.media-left>figure.is-32x32
                [icon :question]]
              [:div.media-content
                [:p.title.is-5 name]
                [:p.subtitle.is-6 tag]]]
            [:div.content
              [:em "This entity has not been linked yet."]
              [:p.control.is-grouped
                [:input.input {:type "text" :placeholder "Search for entities"
                               :value (or @query name)
                               :on-change #(dispatch [:set [:linking-inputs query-key] (ev->val %)])}]
                [:a.button
                  {:on-click #(do
                                (dispatch [:query-gnd (or @query name) tag query-key])
                                (dispatch [:set [:show-suggestions query-key] true]))
                   :class (when loading? "is-loading")}
                  [icon :search]]]]]]
          (when show-suggestions?
            [linking-suggestions query-key])]))))

(defn linking-tool [tags tokens  empty_tag]
  (let [show-suggestions (subscribe [:get :show-suggestions])
        loading (subscribe [:get :loading?])]
    (fn [tags tokens empty_tag]
      [:div.columns.is-multiline
        (doall
          (for [[idx {:keys [tokens tag] :as ent}]
                (-> (extract-entities tokens tags empty_tag) reverse indexed)]
            (let [query-key (str (string/join " " tokens) "." tag)
                  show-suggs? (get @show-suggestions query-key)]
              (log query-key)
              ^{:key idx} [linking-widget ent query-key (get @loading query-key)
                                          show-suggs?])))])))

(defn tagging-panel []
  (let [sentence (subscribe [:get :active-sentence])
        sentences (subscribe [:get :sentences])
        project (subscribe [:active-project])
        fetching? (subscribe [:get :loading? :initial-sentences])]
    (fn []
      (let [{:keys [tagset id]} @project
            {:keys [tags empty_tag]} tagset
            tag-colors (make-tag-colors tagset)]
        [:section.hero.is-fullheight.tagging-container
         [:div.hero-content>div.container
            (if @fetching?
              [:div.loading-spinner]
              [:div
                [tagging-info]
                [tagging-sentence (:tokens @sentence) (:tags @sentence)
                                  tag-colors empty_tag]
                #_(when (has-entities? (:tags @sentence) empty_tag)
                   (do
                     (log "Calling with: " (:tags @sentence))
                     [linking-tool (:tags @sentence) (:tokens @sentence) empty_tag]))
                [tagging-toolbar id (not (empty? @sentences))]])]]))))
