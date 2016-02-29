(ns ^:figwheel-no-load annotare.app
  (:require [annotare.core :as core]
            [devtools.core :as devtools]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)
(devtools/enable-feature! :sanity-hints :dirac)
(devtools/install!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws")

(core/main)
