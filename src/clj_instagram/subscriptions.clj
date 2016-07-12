(ns clj-instagram.subscriptions
  "Functions for creating, deleting and listing subscriptions.

  User subscriptions are useful if you want to be notified when people who authenticated your app post new media on
  Instagram. Subscriptions are not made on behalf of users but using client id and client secret. The implementation of
  subscriptions leverages parts of the Pubsubhubub protocol.

  More information about subscriptions available at https://www.instagram.com/developer/subscriptions/"
  (:refer-clojure :exclude [list])
  (:require [clj-instagram.requests :as requests]
            [clj-instagram.http-client :as http-client]))

(defn create
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
    callback-url  - callback URL (must support both GET and POST)
    opts          - map of clj-http options"
  ([client-id client-secret object aspect verify-token callback-url]
   (create client-id client-secret object aspect verify-token callback-url {}))
  ([client-id client-secret object aspect verify-token callback-url opts]
   (http-client/make-request (requests/create-subscription client-id client-secret object aspect verify-token callback-url)
                             opts)))

(defn list
  "List current subscriptions.

  Parameters:
    client-id     - client id
    client-secret - client secret
    opts          - map of clj-http options"
  ([client-id client-secret]
   (list client-id client-secret {}))
  ([client-id client-secret opts]
   (http-client/make-request (requests/list-subscriptions client-id client-secret) opts)))

(defn delete
  "Delete subscription either by object type or subscription ID. All subscriptions can be deleted by using value \"all\"
  as an object type.

  Parameters:
    client-id     - client id
    client-secret - client secret
    object        - object type or \"all\" (optional, either object type or id must be given)
    id            - subscription id (optional, either object type or id must be given)
    opts          - map of clj-http options"
  ([client-id client-secret object-or-id]
   (delete client-id client-secret object-or-id {}))
  ([client-id client-secret object-or-id opts]
   (http-client/make-request (requests/delete-subscription client-id client-secret object-or-id) opts)))
