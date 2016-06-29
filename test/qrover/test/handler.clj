(ns qrover.test.handler
  (:use clojure.test
        ring.mock.request
        qrover.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/service/qrcodes/emails/1"))]
      (is (= (:status response) 200))
      (is (= (:body response)
             "ACCESS DENIED"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
