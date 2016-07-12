(ns clj-instagram.http-client
  "Instagram API HTTP client that handles common tasks related to requests and responses.

  This includes:
  * Return parsed response body
  * Do not throw on 400 errors
  * Authenticate request with given access token
  * Sign request with given client secret"
  (:require [cheshire.core :as cheshire]
            [clj-instagram.requests :as requests]
            [clj-http.client :as http-client]))

(defn ^:no-doc request-and-catch-400 [req]
  (try
    (http-client/request req)
    (catch Exception e
      (let [{:keys [status] :as response} (ex-data e)]
        (if (= 400 status)
          response
          (throw e))))))

(defn make-request
  "Makes HTTP request and parses JSON response body to Clojure object. Throws an exception on all exceptional HTTP
  status code except 400.

  Parameters:
    req              - clj-http request map
    http-client-opts - map of additional clj-http options"
  [req http-client-opts]
  (-> (request-and-catch-400 (merge req http-client-opts))
      :body
      (cheshire/parse-string true)))

(defn make-endpoint-request
  "Makes HTTP request and parses JSON response body to Clojure object. Throws an exception on all exceptional HTTP
  status code except 400. Authenticates requests with given access token. Be default signs requests with given
  client secret. Signing can be prevented by setting `sig-request?` parameter to false.

  Parameters:
    req              - clj-http request map
    access-token     - a valid access token
    client-secret    - client secret for encoding the request (optional)
    sign-request?    - sign requests (optional, default: true)
    http-client-opts - map of  additional clj-http options"
  [req {:keys [access-token sign-request? client-secret http-client-opts] :or {sign-request? true}}]
  (assert (or client-secret (false? sign-request?)) "Client secret is missing")
  (make-request (cond-> (requests/authenticate req access-token)
                        sign-request? (requests/sign client-secret))
                http-client-opts))
