(ns clj-instagram.oauth-test
  (:require [clj-instagram.oauth :as oauth]
            [midje.sweet :refer :all]))

(fact
  "Simple authorization url is created correctly"
  (oauth/authorization-url "fbc3d555a50e495fbffa24cc2bb6293c" "http://yourcallback.com/")
  => "https://api.instagram.com/oauth/authorize/?client_id=fbc3d555a50e495fbffa24cc2bb6293c&redirect_uri=http%3A%2F%2Fyourcallback.com%2F&response_type=code")

(fact
  "Authorization url with scope (set of keywords) is created correctly"
  (oauth/authorization-url "fbc3d555a50e495fbffa24cc2bb6293c" "http://yourcallback.com/" {:scope #{:relationships :likes}})
  => "https://api.instagram.com/oauth/authorize/?client_id=fbc3d555a50e495fbffa24cc2bb6293c&redirect_uri=http%3A%2F%2Fyourcallback.com%2F&response_type=code&scope=likes+relationships")

(fact
  "Authorization url with scope (strings) is created correctly"
  (oauth/authorization-url "fbc3d555a50e495fbffa24cc2bb6293c" "http://yourcallback.com/" {:scope "public_content follower_list"})
  => "https://api.instagram.com/oauth/authorize/?client_id=fbc3d555a50e495fbffa24cc2bb6293c&redirect_uri=http%3A%2F%2Fyourcallback.com%2F&response_type=code&scope=public_content+follower_list")

(fact
  "Authorization url with state is created correctly"
  (oauth/authorization-url "fbc3d555a50e495fbffa24cc2bb6293c" "http://yourcallback.com/" {:state "1234"})
  => "https://api.instagram.com/oauth/authorize/?client_id=fbc3d555a50e495fbffa24cc2bb6293c&redirect_uri=http%3A%2F%2Fyourcallback.com%2F&response_type=code&state=1234")

(fact
  "Authorization url with scope and state is created correctly"
  (oauth/authorization-url "fbc3d555a50e495fbffa24cc2bb6293c" "http://yourcallback.com/" {:scope #{:relationships :likes} :state "1234"})
  => "https://api.instagram.com/oauth/authorize/?client_id=fbc3d555a50e495fbffa24cc2bb6293c&redirect_uri=http%3A%2F%2Fyourcallback.com%2F&response_type=code&state=1234&scope=likes+relationships")
