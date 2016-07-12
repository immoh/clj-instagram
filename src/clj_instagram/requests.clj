(ns clj-instagram.requests
  "Pure functions for creating and transforming request maps for accessing Instagram API.

  Returned request maps are compatible with clj-http library (https://github.com/dakrone/clj-http) but should be usable
  with any HTTP client library with minor modifications. The following keys/values are used:

    :method       - HTTP method; :get, :post or :delete
    :url          - URL (string)
    :query-params - map of query params with keyword keys
                    (optional, applicable for GET and DELETE requests)
    :form-params  - map of form params with keyword keys
                    (optional, applicable for POST requests)


  For more details about the API refer to official documentation:

  * Authentication: https://www.instagram.com/developer/authentication/
  * Endpoints: https://www.instagram.com/developer/endpoints/
  * Subscriptions: https://www.instagram.com/developer/subscriptions/"
  (:require [buddy.core.codecs :as codecs]
            [buddy.core.mac :as mac]
            [clojure.string]))

(def ^:no-doc api-url-base "https://api.instagram.com/v1")

;; OAuth

(defn request-access-token
  "Exchange code received from Instagram for an access token.

  Parameters:
    client-id     - client ID
    client-secret - client secret
    redirect-uri  - the redirect URI used in the authorization request
    code          - the exact code received during the authorization step"
  [client-id client-secret redirect-uri code]
  {:url         "https://api.instagram.com/oauth/access_token"
   :method      :post
   :form-params {:client_id      client-id
                 :client_secret  client-secret
                 :redirect_uri   redirect-uri
                 :grant_type     "authorization_code"
                 :code           code}})

;; Users

(defn get-user
  "Get information about a user. If user id is omitted returns information about the owner of the access token.

  Parameters:
    user-id - user id (default: owner of the access token)"
  ([]
   (get-user "self"))
  ([user-id]
   {:method :get
    :url    (format "%s/users/%s" api-url-base user-id)}))

(defn get-recent-media-by-user
  "Get the most recent media published by a user.

  Parameters:
    user-id - user id
    count   - count of media to return (optional)
    min-id  - return media later than this min id (optional)
    max-id  - return media earlier than this max id (optional)"
  ([user-id]
    (get-recent-media-by-user user-id {}))
  ([user-id {:keys [count min-id max-id]}]
   {:method       :get
    :url          (format "%s/users/%s/media/recent" api-url-base user-id)
    :query-params {:count  count
                   :min_id min-id
                   :max_id max-id}}))

(defn get-recent-media-by-me
  "Get the most recent media published by the owner of the access token.

  Parameters:
    count   - count of media to return (optional)
    min-id  - Return media later than this min id (optional)
    max-id  - Return media earlier than this max id (optional)"
  ([]
    (get-recent-media-by-user "self"))
  ([{:keys [count min-id max-id] :as optional-params}]
    (get-recent-media-by-user "self" optional-params)))

(defn get-liked-media
  "Get the list of recent media liked by the owner of the access token.

  Parameters:
    count       - count of media to return (optional)
    max-like-id - return media liked before this id (optional)"
  ([]
   (get-liked-media {}))
  ([{:keys [count max-like-id]}]
   {:method       :get
    :url          (format "%s/users/self/media/liked" api-url-base)
    :query-params {:count       count
                   :max_like_id max-like-id}}))

(defn search-users
  "Get a list of users matching the query.

  Parameters:
    q     - a query string
    count - number of users to return (optional)"
  ([q]
   (search-users q nil))
  ([q count]
   {:method       :get
    :url          (format "%s/users/search" api-url-base)
    :query-params {:q     q
                   :count count}}))

;; Relationships

(defn get-follows
  "Get the list of users this user follows."
  []
  {:method :get
   :url    (format "%s/users/self/follows" api-url-base)})

(defn get-followed-by
  "Get the list of users this user is followed by."
  []
  {:method :get
   :url    (format "%s/users/self/followed-by" api-url-base)})

(defn get-requested-by
  "List the users who have requested this user's permission to follow."
  []
  {:method :get
   :url    (format "%s/users/self/requested-by" api-url-base)})

(defn get-relationship
  "Get information about a relationship to another user. Relationships are expressed using the following terms in the
  response:

  * outgoing_status: Your relationship to the user. Can be 'follows', 'requested', 'none'
  * incoming_status: A user's relationship to you. Can be 'followed_by', 'requested_by', 'blocked_by_you', 'none'"
  [user-id]
  {:method :get
   :url    (format "%s/users/%s/relationship" api-url-base user-id)})

(defn modify-relationship
  "Modify the relationship between the current user and the target user. Relationships are expressed using the following
  terms in the response:

  * outgoing_status: Your relationship to the user. Can be 'follows', 'requested', 'none'
  * incoming_status: A user's relationship to you. Can be 'followed_by', 'requested_by', 'blocked_by_you', 'none'

  Parameters:
    user-id - user id
    action  - action to perform: :follow, :unfollow, :approve or :ignore"
  [user-id action]
  {:method      :post
   :url         (format "%s/users/%s/relationship" api-url-base user-id)
   :form-params {:action (name action)}})

;; Media

(defn get-media-by-id
  "Get information about a media object. Use the :type field to differentiate between image and video media in the
  response. You will also receive the :user_has_liked field which tells you whether the owner of the access token has
  liked this media."
  [media-id]
  {:method :get
   :url    (format "%s/media/%s" api-url-base media-id)})

(defn get-media-by-shortcode
  "Get information about a media object. Use the :type field to differentiate between image and video media in the
  response. You will also receive the :user_has_liked field which tells you whether the owner of the access token has
  liked this media. A media object's shortcode can be found in its shortlink URL. An example shortlink is
  http://instagram.com/p/tsxp1hhQTG/. Its corresponding shortcode is tsxp1hhQTG."
  [shortcode]
  {:method :get
   :url    (format "%s/media/shortcode/%s" api-url-base shortcode)})

(defn search-media
  "Search for recent media in a given area.

  Parameters:
    lat      - latitude of the center search coordinate
    lng      - longitude of the center search coordinate
    distance - distance in meters (optional, default 1000, max 5000)"
  ([lat lng]
    (search-media lat lng nil))
  ([lat lng distance]
   {:method       :get
    :url          (format "%s/media/search" api-url-base)
    :query-params {:lat      lat
                   :lng      lng
                   :distance distance}}))

;; Comments

(defn get-comments
  "Get a list of recent comments on a media object."
  [media-id]
  {:method :get
   :url    (format "%s/media/%s/comments" api-url-base media-id)})

(defn create-comment
  "Create a comment on a media object with the following rules:

  * The total length of the comment cannot exceed 300 characters
  * The comment cannot contain more than 4 hashtags
  * The comment cannot contain more than 1 URL
  * The comment cannot consist of all capital letters

  Parameters:
    media-id - media id
    text     - comment text"
  [media-id text]
  {:method      :post
   :url         (format "%s/media/%s/comments" api-url-base media-id)
   :form-params {:text text}})

(defn delete-comment
  "Remove a comment either on the authenticated user's media object or authored by the authenticated user.

  Parameters:
    media-id   - media id
    comment-id - comment id"
  [media-id comment-id]
  {:method :delete
   :url    (format "%s/media/%s/comments/%s" api-url-base media-id comment-id)})

;; Likes

(defn get-likes
  "Get a list of users who have liked this media."
  [media-id]
  {:method :get
   :url    (format "%s/media/%s/likes" api-url-base media-id)})

(defn set-like
  "Set a like on this media by the currently authenticated user."
  [media-id]
  {:method :post
   :url    (format "%s/media/%s/likes" api-url-base media-id)})

(defn remove-like
  "Remove a like on this media by the currently authenticated user."
  [media-id]
  {:method :delete
   :url    (format "%s/media/%s/likes" api-url-base media-id)})

;; Tags

(defn get-tag
  "Get information about a tag object."
  [tag]
  {:method :get
   :url    (format "%s/tags/%s" api-url-base tag)})

(defn get-recently-tagged-media
  "Get a list of recently tagged media.

  Parameters:
    tag        - tag name
    count      - count of tagged media to return (optional)
    min-tag-id - return media before this min tag id (optional)
    max-tag-id - return media after this max tag id (optional)"
  ([tag]
   (get-recently-tagged-media tag {}))
  ([tag {:keys [count min-tag-id max-tag-id]}]
   {:method       :get
    :url          (format "%s/tags/%s/media/recent" api-url-base tag)
    :query-params {:count      count
                   :min_tag_id min-tag-id
                   :max_tag_id max-tag-id}}))

(defn search-tags
  "Search for tags by name.

  Parameters:
    q - a valid tag name without a leading # (eg. snowy, nofilter)"
  [q]
  {:method       :get
   :url          (format "%s/tags/search" api-url-base)
   :query-params {:q q}})

;; Locations

(defn get-location
  "Get information about a location."
  [location-id]
  {:method :get
   :url    (format "%s/locations/%s" api-url-base location-id)})

(defn get-recent-media-from-location
  "Get a list of recent media objects from a given location.

  Parameters:
    location-id - location id
    min-id      - return media before this min id (optional)
    max-id      - return media after this max id (optional)"
  ([location-id]
   (get-recent-media-from-location location-id {}))
  ([location-id {:keys [min-id max-id]}]
   {:method       :get
    :url          (format "%s/locations/%s/media/recent" api-url-base location-id)
    :query-params {:min_id min-id
                   :max_íd max-id}}))

(defn search-locations
  "Search for a location by geographic coordinate or Facebook Places ID.

  Parameters:
    facebook-places-id - returns a location mapped off of a Facebook places id.
                         (optional, if used, lat and lng are not required)
    lat                - latitude of the center search coordinate (optional, if used, lng is required)
    lng                - longitude of the center search coordinate (optional, if used, lat is required)
    distance           - distance in meters (optional, default 1000, max 5000)"
  [{:keys [facebook-places-id lat lng distance]}]
  {:method       :get
   :url          (format "%s/locations/search" api-url-base)
   :query-params {:facebook_places_id facebook-places-id
                  :lat                lat
                  :lng                lng
                  :distance           distance}})

;; Subscriptions

(defn create-subscription
  "Create subscription to given aspect of the given object.

  Simultaneously submits a GET request to the provided callback URL to verify its existence with the following
  parameters:

  * hub.mode         - this will be set to \"subscribe\"
  * hub.challenge    - this will be set to a random string that your callback URL will need to echo back in order to
                       verify you'd like to subscribe
  * hub.verify_token - this will be set to whatever verify token passed in with the subscription request,
                       it's helpful to use this to differentiate between multiple subscription requests

  In order to verify the subscription, your server must respond to the GET request with the hub.challenge parameter
  only.

  When new data arrives, the given callback URL will receive a POST request with a payload containing the updates.

  Parameters:
    client-id     - client id
    client-secret - client secret
    object        - the object to subscribe to (e.g. 'user')
    aspect        - the aspect of the object to subscribe to (e.g. 'media')
    verify-token  - verify token to use in the Pubsubhubub challenge flow
    callback-url  - callback URL (must support both GET and POST)"
  [client-id client-secret object aspect verify-token callback-url]
  {:url         (format "%s/subscriptions" api-url-base)
   :method      :post
   :form-params {:client_id     client-id
                 :client_secret client-secret
                 :object        object
                 :aspect        aspect
                 :verify_token  verify-token
                 :callback_url  callback-url}})

(defn list-subscriptions
  "List current subscriptions.

  Parameters:
    client-id     - client id
    client-secret - client secret"
  [client-id client-secret]
  {:url          (format "%s/subscriptions" api-url-base)
   :method       :get
   :query-params {:client_id     client-id
                  :client_secret client-secret}})

(defn delete-subscription
  "Delete subscription either by object type or subscription ID. All subscriptions can be deleted by using value \"all\"
  as an object type.

  Parameters:
    client-id     - client id
    client-secret - client secret
    object        - object type or \"all\" (optional, either object type or id must be given)
    id            - subscription id (optional, either object type or id must be given)"
  [client-id client-secret {:keys [object id]}]
  {:url          (format "%s/subscriptions" api-url-base)
   :method       :delete
   :query-params {:client_id     client-id
                  :client_secret client-secret
                  :object        object
                  :id            id}})

;; Transform

(defn ^:no-doc params-key [{:keys [method]}]
  (if (= :post method) :form-params :query-params))

(defn authenticate
  "Adds access token to the given request. Depending on the request method, access token is added either to query params
  or form params."
  [req access-token]
  (assoc-in req [(params-key req) :access_token] access-token))

(defn ^:no-doc params->str [params]
  (clojure.string/join "|" (map (fn [[k v]] (format "%s=%s" (name k) v)) (sort params))))

(defn ^:no-doc extract-endpoint [url]
  (clojure.string/replace url api-url-base ""))

(defn ^:no-doc compute-sig [url params client-secret]
  (-> (format "%s|%s" (extract-endpoint url) (params->str params))
      (mac/hash {:key client-secret :alg :hmac+sha256})
      (codecs/bytes->hex)))

(defn sign
  "Computes and adds signature to the request using given client secret."
  [{:keys [url] :as req} client-secret]
  (let [params-key (params-key req)]
    (assoc-in req [params-key :sig] (compute-sig url (get req params-key) client-secret))))
