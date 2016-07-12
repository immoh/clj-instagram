(ns clj-instagram.oauth
  "Functions for creating an authorization URL and exchanging code received from Instagram for an access token.

  For more details refer to official documentation about authentication:
  https://www.instagram.com/developer/authentication/"
  (:require [clj-instagram.requests :as requests]
            [clj-instagram.http-client :as http-client]))

(defn ^:no-doc url-encode [s]
  (java.net.URLEncoder/encode s "UTF-8"))

(defn ^:no-doc query-params [m]
  (clojure.string/join "&" (map (fn [[k v]] (format "%s=%s" (url-encode (name k)) (url-encode v))) m)))

(defn ^:no-doc scope->str [scope]
  (if (coll? scope)
    (clojure.string/join " " (map name scope))
    scope))

(defn authorization-url
  "Creates an authorization URL that will allow user to log in and grant your application access to user's Instagram
  data.

  Parameters:
    client-id    - client id
    redirect-uri - location of your application user will be redirected to
    state        - any server-specific state you want to carry through (optional)
    scope        - request additional permissions outside the \"basic\" scope (optional)"
  ([client-id redirect-uri]
    (authorization-url client-id redirect-uri {}))
  ([client-id redirect-uri {:keys [state scope]}]
   (format "https://api.instagram.com/oauth/authorize/?%s"
           (query-params (merge {:client_id     client-id
                                 :redirect_uri  redirect-uri
                                 :response_type "code"}
                                (when state {:state state})
                                (when scope {:scope (scope->str scope)}))))))

(defn request-access-token
  "Exchange code received from Instagram for an access token.

  Parameters:
    client-id     - client ID
    client-secret - client secret
    redirect-uri  - the redirect URI used in the authorization request
    code          - the exact code received during the authorization step
    opts          - HTTP client options (optional)"
  ([client-id client-secret redirect-uri code]
   (request-access-token client-id client-secret redirect-uri code {}))
  ([client-id client-secret redirect-uri code opts]
   (http-client/make-request (requests/request-access-token client-id client-secret redirect-uri code) opts)))
