(ns annotare.tagging
  (:require [reagent.core :as r]
            [goog.events :as events]
            [ajax.core :refer [GET POST]]))

;; Offscreen-Canvas for determining text-width
(def offscreen-canvas (.createElement js/document "canvas"))

(defn get-text-width [text font font-size]
  (let [ctx (.getContext offscreen-canvas "2d")]
    (set! (.-font ctx) (str font-size " " font))
    (.debug js/console (str "font is ") (.-font ctx))
    (.debug js/console (str "width of text '" text "' is " (.-width (.measureText ctx text))))
    (.-width (.measureText ctx text))))

(defn tagging-token [token current-tag tag-set]
  (let [text-width (get-text-width token "Helvetica Neue" "56px")
        tag-width (get-text-width (apply (partial max-key count) tag-set) "Helvetica Neue" "14px")
        select-width (+ text-width tag-width)]
    [:div.tag-select {:style {:margin-right (str (* 1.25 tag-width) "px")}}
      [:select {:style {:width (str select-width "px")
                        :padding-left (str text-width "px")}
                :default-value current-tag}
        (for [tag tag-set]
          ^{:key tag} [:option tag])]
      [:span.token {:style {:margin-left (str "-" select-width "px")}} token]]))
