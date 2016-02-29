(ns annotare.macros)

(defmacro log [& args]
  `(.log js/console ~@args))

(defmacro warn [& args]
  `(.warn js/console ~@args))

(defmacro error [& args]
  `(.warn js/console ~@args))

(defmacro group [& args]
  `(if (.-groupCollapsed js/console)
    (.groupCollapsed js/console ~@args)
    (.log js/console ~@args)))
