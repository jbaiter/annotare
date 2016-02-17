(ns annotare.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [annotare.core-test]))

(doo-tests 'annotare.core-test)

