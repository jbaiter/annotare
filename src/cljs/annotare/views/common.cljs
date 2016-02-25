(ns annotare.views.common)

(defn icon [type size]
  [:span.icon>i.fa {:class [(str "fa-" (name type) (when size (str " is-" (name size))))]}])
