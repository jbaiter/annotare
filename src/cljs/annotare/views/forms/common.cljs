(ns annotare.views.forms.common)

(defn form-field [title & children]
  [:p.control
   [:label.label title]
   (map-indexed #(with-meta %2  {:key %1}) children)])
