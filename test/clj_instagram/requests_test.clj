(ns clj-instagram.requests-test
  (:require [clj-instagram.requests :as requests]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [midje.sweet :refer :all]))

(def request-fn-vars [
                      ;; OAuth
                      #'requests/request-access-token
                      ;; Users
                      #'requests/get-user
                      #'requests/get-recent-media-by-user
                      #'requests/get-recent-media-by-me
                      #'requests/get-liked-media
                      #'requests/search-users
                      ;; Relationships
                      #'requests/get-follows
                      #'requests/get-followed-by
                      #'requests/get-requested-by
                      #'requests/get-relationship
                      #'requests/modify-relationship
                      ;; Media
                      #'requests/get-media-by-id
                      #'requests/get-media-by-shortcode
                      #'requests/search-media
                      ;; Comments
                      #'requests/get-comments
                      #'requests/create-comment
                      #'requests/delete-comment
                      ;; Likes
                      #'requests/get-likes
                      #'requests/set-like
                      #'requests/remove-like
                      ;; Tags
                      #'requests/get-tag
                      #'requests/get-recently-tagged-media
                      #'requests/search-tags
                      ;; Locations
                      #'requests/get-location
                      #'requests/get-recent-media-from-location
                      #'requests/search-locations
                      ;; Subscriptions
                      #'requests/create-subscription
                      #'requests/list-subscriptions
                      #'requests/delete-subscription
                      ])


(def gen-fn-var+arglist
  (gen/bind
    (gen/elements request-fn-vars)
    (fn [var]
      (gen/tuple
        (gen/return var)
        (gen/elements (:arglists (meta var)))))))

(defn ->generator [arg]
  (if (map? arg)
    (let [ks (map keyword (:keys arg))]
      (gen/map (gen/elements ks) gen/string))
    gen/string))

(defn gen-args [arglist]
  (apply gen/tuple (map ->generator arglist)))

(defn gen-call [var arglist]
  (gen/fmap (fn [args]
              (apply var args))
            (gen-args arglist)))

(def gen-req
  (gen/bind
    gen-fn-var+arglist
    (fn [[var arglist]]
      (gen-call var arglist))))

(defspec
  request-url-is-instagram-api-url 100
  (prop/for-all
    [req gen-req]
    (.startsWith (:url req) "https://api.instagram.com/")))

(defspec
  request-method-is-valid 100
  (prop/for-all
    [req gen-req]
    (#{:get :post :delete} (:method req))))

(defspec
  request-has-valid-key-combination 100
  (prop/for-all
    [req gen-req]
    (let [method (:method req)
          ks (set (keys req))]
      (or
        (and
          (= :post method)
          (#{#{:url :method} #{:url :method :form-params}} ks))
        (and
          (not= :post method)
          (#{#{:url :method} #{:url :method :query-params}} ks))))))

(defspec
  query-params-is-map-if-exists 100
  (prop/for-all
    [req gen-req]
    (or (not (contains? req :query-params))
        (map? (:query-params req)))))

(defspec
  form-params-is-map-if-exists 100
  (prop/for-all
    [req gen-req]
    (or (not (contains? req :form-params))
        (map? (:form-params req)))))


(fact
  "Access token is added to query params for GET request"
  (requests/authenticate (requests/get-likes "1234567894561231236_33215652")
                         "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
  => (contains {:query-params (contains {:access_token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"})}))


(fact
  "Access token is added to form params for POST request"
  (requests/authenticate (requests/set-like "1234567894561231236_33215652")
                         "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
  => (contains {:form-params (contains {:access_token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"})}))

(fact
  "Access token is added to query params for DELETE request"
  (requests/authenticate (requests/remove-like "1234567894561231236_33215652")
                         "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
  => (contains {:query-params (contains {:access_token "fb2e77d.47a0479900504cb3ab4a1f626d174d2d"})}))

(fact
  "Signature is added to query params for GET request"
  (-> (requests/get-user)
      (requests/authenticate "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
      (requests/sign "6dc1787668c64c939929c17683d7cb74"))
  => (contains {:query-params (contains {:sig "cbf5a1f41db44412506cb6563a3218b50f45a710c7a8a65a3e9b18315bb338bf"})}))


(fact
  "Signature is added to form params for POST request"
  (-> (requests/create-comment "1234567894561231236_33215652" "Great photo!")
      (requests/authenticate "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
      (requests/sign "6dc1787668c64c939929c17683d7cb74"))
  => (contains {:form-params (contains {:sig "26efbe3c43c5e3927d98d4a2adae1c4e751e47495bded47d94dee435f7f9d7df"})}))

(fact
  "Signature is added to form params for DELETE request"
  (-> (requests/remove-like "1234567894561231236_33215652")
      (requests/authenticate "fb2e77d.47a0479900504cb3ab4a1f626d174d2d")
      (requests/sign "6dc1787668c64c939929c17683d7cb74"))
  => (contains {:query-params (contains {:sig "cf10e2d50bf54b7e83370645719136c60a82f5b3e3c6593bd7f145340ca2a6c8"})}))
