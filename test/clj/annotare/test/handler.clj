(ns annotare.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [annotare.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "all roads lead to main"
    (let [response (app (request :get "/invalid"))]
      (is (= 200 (:status response))))))
