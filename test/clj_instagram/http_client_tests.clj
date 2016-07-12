(ns clj-instagram.http-client-tests
  (:require [midje.sweet :refer :all]
            [clj-instagram.http-client :as http-client]
            [clj-http.client])
  (:import (clojure.lang ExceptionInfo)))

(fact
  "make-request returns parsed response body"
  (http-client/make-request {:method :get :url "https://api.instagram.com/users/self"} {})
  => {:meta {:code 200}
      :data {:username "immoh"}}
  (provided
    (clj-http.client/request {:method :get :url "https://api.instagram.com/users/self"})
    => {:body "{\"meta\": {\"code\": 200}, \"data\": {\"username\": \"immoh\"}}"}))

(fact
  "make-request doesn't throw on 400 responses"
  (http-client/make-request {:method :get :url "https://api.instagram.com/users/self"} {})
  => {:meta {:code 400 :error "No such user"}}
  (provided
    (clj-http.client/request {:method :get :url "https://api.instagram.com/users/self"})
    =throws=> (ex-info nil {:status 400 :body "{\"meta\": {\"code\": 400, \"error\": \"No such user\"}}"})))

(fact
  "make-request throws on other error responses than 400"
  (http-client/make-request {:method :get :url "https://api.instagram.com/users/self"} {})
  => (throws ExceptionInfo)
  (provided
    (clj-http.client/request {:method :get :url "https://api.instagram.com/users/self"})
    =throws=> (ex-info nil {:status 404 :body "Not found"})))

(fact
  "make-request passes additional options to clj-http.client"
  (http-client/make-request {:method :get :url "https://api.instagram.com/users/self"} {:debug true})
  => anything
  (provided
    (clj-http.client/request {:method :get :url "https://api.instagram.com/users/self" :debug true})
    => anything))

(fact
  "make-api-request adds access token and signature"
  (http-client/make-endpoint-request {:method :get :url "https://api.instagram.com/users/self"}
                                     {:access-token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"
                                      :client-secret "6dc1787668c64c939929c17683d7cb74"})
  => anything
  (provided
    (clj-http.client/request {:method :get
                              :url "https://api.instagram.com/users/self"
                              :query-params {:access_token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"
                                             :sig "bc9b0942861ff6d422f6dcb1a140dbd754b4783cffe24dd0d293451e3f82ea89"}})
    => anything))

(fact
  "make-api-request throws exception if client secret is missing"
  (http-client/make-endpoint-request {:method :get :url "https://api.instagram.com/users/self"}
                                     {:access-token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"})
  => (throws AssertionError #"Client secret is missing"))

(fact
  "make-api-request doesn't add signature if sign-request? parameter is false"
  (http-client/make-endpoint-request {:method :get :url "https://api.instagram.com/users/self"}
                                     {:access-token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"
                                      :sign-request? false})
  => anything
  (provided
    (clj-http.client/request {:method :get
                              :url "https://api.instagram.com/users/self"
                              :query-params {:access_token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"}})
    => anything))