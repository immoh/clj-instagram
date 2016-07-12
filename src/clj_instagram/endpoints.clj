(ns clj-instagram.endpoints
  "Functions for accessing Instagram endpoints. The API requires access from authenticated users for each endpoint.
  Functions in this namespace are using corresponding functions from `clj-instagram.requests` namespace.

  They require one additional map parameter with the following possible keys:

    :access-token     - a valid access token
    :client-secret    - client secret for encoding the request (optional)
    :sign-request?    - sign requests (optional, default: true)
    :http-client-opts - map of HTTP client options (optional)

  For more details about available HTTP client options: https://github.com/dakrone/clj-http

  For more details about the API refer to official documentation: https://www.instagram.com/developer/endpoints/"
  (:require [clj-instagram.http-client :as http-client]
            [clj-instagram.requests :as requests]))

;; Users

(defn get-user
  "Get information about a user. If user id is omitted returns information about the owner of the access token.

  Parameters:
    user-id - user id (default: owner of the access token)
    opts    - map of endpoint call options"
  ([opts]
   (get-user "self" opts))
  ([user-id opts]
   (http-client/make-endpoint-request (requests/get-user user-id) opts)))

(defn get-recent-media-by-user
  "Get the most recent media published by a user.

  Parameters:
    user-id - user id
    count   - count of media to return (optional)
    min-id  - return media later than this min id (optional)
    max-id  - return media earlier than this max id (optional)
    opts    - map of endpoint call options"
  ([user-id opts]
   (get-recent-media-by-user user-id {} opts))
  ([user-id optional-params opts]
   (http-client/make-endpoint-request (requests/get-recent-media-by-user user-id optional-params) opts)))

(defn get-recent-media-by-me
  "Get the most recent media published by the owner of the access token.

  Parameters:
    count   - count of media to return (optional)
    min-id  - Return media later than this min id (optional)
    max-id  - Return media earlier than this max id (optional)
    opts    - map of endpoint call options"
  ([opts]
    (get-recent-media-by-user "self" opts))
  ([optional-params opts]
    (get-recent-media-by-user "self" optional-params opts)))

(defn get-liked-media
  "Get the list of recent media liked by the owner of the access token.

  Parameters:
    count       - count of media to return (optional)
    max-like-id - return media liked before this id (optional)
    opts        - map of endpoint call options"
  ([opts]
   (get-liked-media {} opts))
  ([optional-params opts]
   (http-client/make-endpoint-request (requests/get-liked-media optional-params) opts)))

(defn search-users
  "Get a list of users matching the query.

  Parameters:
    q     - a query string
    count - number of users to return (optional)
    opts  - map of endpoint call options"
  ([q opts]
   (search-users q nil opts))
  ([q count opts]
   (http-client/make-endpoint-request (requests/search-users q count) opts)))

;; Relationships

(defn get-follows
  "Get the list of users this user follows.

  Parameters:
    opts - map of endpoint call options"
  [opts]
  (http-client/make-endpoint-request (requests/get-follows) opts))

(defn get-followed-by
  "Get the list of users this user is followed by.

  Parameters:
    opts - map of endpoint call options"
  [opts]
  (http-client/make-endpoint-request (requests/get-followed-by) opts))

(defn get-requested-by
  "List the users who have requested this user's permission to follow.

  Parameters:
    opts - map of endpoint call options"
  [opts]
  (http-client/make-endpoint-request (requests/get-requested-by) opts))

(defn get-relationship
  "Get information about a relationship to another user. Relationships are expressed using the following terms in the
  response:

  * outgoing_status: Your relationship to the user. Can be 'follows', 'requested', 'none'
  * incoming_status: A user's relationship to you. Can be 'followed_by', 'requested_by', 'blocked_by_you', 'none'

  Parameters:
    user-id - user id
    opts    - map of endpoint call options"
  [user-id opts]
  (http-client/make-endpoint-request (requests/get-relationship user-id) opts))

(defn modify-relationship
  "Modify the relationship between the current user and the target user. Relationships are expressed using the following
  terms in the response:

  * outgoing_status: Your relationship to the user. Can be 'follows', 'requested', 'none'
  * incoming_status: A user's relationship to you. Can be 'followed_by', 'requested_by', 'blocked_by_you', 'none'

  Parameters:
    user-id - user id
    action  - action to perform: :follow, :unfollow, :approve or :ignore
    opts    - map of endpoint call options"
  [user-id action opts]
  (http-client/make-endpoint-request (requests/modify-relationship user-id action) opts))

;; Media

(defn get-media-by-id
  "Get information about a media object. Use the :type field to differentiate between image and video media in the
  response. You will also receive the :user_has_liked field which tells you whether the owner of the access token has
  liked this media.

  Parameters:
    media-id - media id
    opts     - map of endpoint call options"
  [media-id opts]
  (http-client/make-endpoint-request (requests/get-media-by-id media-id) opts))

(defn get-media-by-shortcode
  "Get information about a media object. Use the :type field to differentiate between image and video media in the
  response. You will also receive the :user_has_liked field which tells you whether the owner of the access token has
  liked this media. A media object's shortcode can be found in its shortlink URL. An example shortlink is
  http://instagram.com/p/tsxp1hhQTG/. Its corresponding shortcode is tsxp1hhQTG.

  Parameters:
    shortcode - media object's shortcode
    opts      - map of endpoint call options"
  [shortcode opts]
  (http-client/make-endpoint-request (requests/get-media-by-shortcode shortcode) opts))

(defn search-media
  "Search for recent media in a given area.

  Parameters:
    lat      - latitude of the center search coordinate
    lng      - longitude of the center search coordinate
    distance - distance in meters (optional, default 1000, max 5000)
    opts     - map of endpoint call options"
  ([lat lng opts]
    (search-media lat lng nil opts))
  ([lat lng distance opts]
   (http-client/make-endpoint-request (requests/search-media lat lng distance) opts)))

;; Comments

(defn get-comments
  "Get a list of recent comments on a media object.

  Parameters:
    media-id - media id
    opts     - map of endpoint call options"
  [media-id opts]
  (http-client/make-endpoint-request (requests/get-comments media-id) opts))

(defn create-comment
  "Create a comment on a media object with the following rules:

  * The total length of the comment cannot exceed 300 characters
  * The comment cannot contain more than 4 hashtags
  * The comment cannot contain more than 1 URL
  * The comment cannot consist of all capital letters

  Parameters:
    media-id - media id
    text     - comment text
    opts     - map of endpoint call options"
  [media-id text opts]
  (http-client/make-endpoint-request (requests/create-comment media-id text) opts))

(defn delete-comment [media-id comment-id opts]
  "Remove a comment either on the authenticated user's media object or authored by the authenticated user.

  Parameters:
    media-id   - media id
    comment-id - comment id
    opts       - map of endpoint call options"
  (http-client/make-endpoint-request (requests/delete-comment media-id comment-id) opts))

;; Likes

(defn get-likes
  "Get a list of users who have liked this media.

  Parameters:
    media-id - media id
    opts     - map of endpoint call options"
  [media-id opts]
  (http-client/make-endpoint-request (requests/get-likes media-id) opts))

(defn set-like
  "Set a like on this media by the currently authenticated user.

  Parameters:
    media-id - media id
    opts     - map of endpoint call options"
  [media-id opts]
  (http-client/make-endpoint-request (requests/set-like media-id) opts))

(defn remove-like
  "Remove a like on this media by the currently authenticated user.

  Parameters:
    media-id - media id
    opts     - map of endpoint call options"
  [media-id opts]
  (http-client/make-endpoint-request (requests/remove-like media-id) opts))

;; Tags

(defn get-tag
  "Get information about a tag object.

  Parameters:
    tag  - tag
    opts - map of endpoint call options"
  [tag opts]
  (http-client/make-endpoint-request (requests/get-tag tag) opts))

(defn get-recently-tagged-media
  "Get a list of recently tagged media.

  Parameters:
    tag        - tag name
    count      - count of tagged media to return (optional)
    min-tag-id - return media before this min tag id (optional)
    max-tag-id - return media after this max tag id (optional)
    opts       - map of endpoint call options"
  ([tag opts]
   (get-recently-tagged-media tag {} opts))
  ([tag {:keys [count min-tag-id max-tag-id] :as optional-params} opts]
   (http-client/make-endpoint-request (requests/get-recently-tagged-media tag optional-params) opts)))

(defn search-tags
  "Search for tags by name.

  Parameters:
    q    - a valid tag name without a leading # (eg. snowy, nofilter)
    opts - map of endpoint call options"
  [q opts]
  (http-client/make-endpoint-request (requests/search-tags q) opts))

;; Locations

(defn get-location
  "Get information about a location.

  Parameters:
    location-id - location id
    opts        - map of endpoint call options"
  [location-id opts]
  (http-client/make-endpoint-request (requests/get-location location-id) opts))

(defn get-recent-media-from-location
  "Get a list of recent media objects from a given location.

  Parameters:
    location-id - location id
    min-id      - return media before this min id (optional)
    max-id      - return media after this max id (optional)
    opts        - map of endpoint call options"
  ([location-id opts]
    (get-recent-media-from-location location-id {} opts))
  ([location-id {:keys [min-id max-id] :as optional-params} opts]
   (http-client/make-endpoint-request (requests/get-recent-media-from-location location-id optional-params) opts)))

(defn search-locations
  "Search for a location by geographic coordinate or Facebook Places ID.

  Parameters:
    facebook-places-id - returns a location mapped off of a Facebook places id.
                         (optional, if used, lat and lng are not required)
    lat                - latitude of the center search coordinate (optional, if used, lng is required)
    lng                - longitude of the center search coordinate (optional, if used, lat is required)
    distance           - distance in meters (optional, default 1000, max 5000)
    opts               - map of endpoint call options"
  [{:keys [facebook-places-id lat lng distance] :as search-params} opts]
  (http-client/make-endpoint-request (requests/search-locations search-params) opts))
