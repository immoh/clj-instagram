(ns clj-instagram.endpoints-test
  (:require [midje.sweet :refer :all]
            [clj-instagram.endpoints :as endpoints]))

(def opts {:access-token (System/getenv "CLJ_INSTAGRAM_ACCESS_TOKEN")
           :client-secret (System/getenv "CLJ_INSTAGRAM_CLIENT_SECRET")})

;; Users

(fact
  "Get user (self)" :integration
  (endpoints/get-user opts)
  => (contains {:meta {:code 200}
                :data (contains {:username anything})}))

(fact
  "Get user (user id)" :integration
  (endpoints/get-user "2987617812" opts)
  => (contains {:meta {:code 200}
                :data (contains {:username "cljtest1"})}))

(fact
  "Get recent media by me" :integration
  (endpoints/get-recent-media-by-me opts)
  => (contains {:meta {:code 200}
                :data (contains [])}))

(fact
  "Get recent media by me with optional params" :integration
  (endpoints/get-recent-media-by-me {:count 1} opts)
  => (contains {:meta {:code 200}
                :data (contains [])}))

(fact
  "Get recent media by user (user id)" :integration
  (endpoints/get-recent-media-by-user "2987617812" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Get recent media by user (user id) with optional params" :integration
  (endpoints/get-recent-media-by-user "2987617812" {:min-id "1196893517451621714_2987617812"} opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Get liked media" :integration
  (endpoints/get-liked-media opts)
  => (contains {:meta {:code 200}
                :data (contains [])}))

(fact
  "Get liked media with optional params" :integration
  (endpoints/get-liked-media {:count 1} opts)
  => (contains {:meta {:code 200}
                :data (contains [])}))

(fact
  "Search users" :integration
  (endpoints/search-users "cljtest" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:username "cljtest1"})])}))

;; Relationships

(fact
  "Get follows" :integration
  (endpoints/get-follows opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:username "cljtest2"})])}))

(fact
  "Get followed by" :integration
  (endpoints/get-followed-by opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:username "cljtest2"})])}))

(fact
  "Get requested by" :integration
  (endpoints/get-requested-by opts)
  => {:meta {:code 200}
      :data []})

(fact
  "Get relationship" :integration
  (endpoints/get-relationship "2987620865" opts)
  => (contains {:meta {:code 200}
                :data (contains {:outgoing_status "follows"})}))

(fact
  "Modify relationship" :integration
  (endpoints/modify-relationship "2987620865" :unfollow opts)
  => (contains {:meta {:code 200}
                :data (contains {:outgoing_status "none"})})
  (endpoints/modify-relationship "2987620865" :follow opts)
  => (contains {:meta {:code 200}
                :data (contains {:outgoing_status "follows"})}))

;; Media

(fact
  "Get media by id" :integration
  (endpoints/get-media-by-id "1196893517451621714_2987617812" opts)
  => (contains {:meta {:code 200}
                :data (contains {:type "image"})}))

(fact
  "Get media by shortcode" :integration
  (endpoints/get-media-by-shortcode "BCcOFANq_VS" opts)
  => (contains {:meta {:code 200}
                :data (contains {:type "image"})}))

(fact
  "Search media" :integration
  (endpoints/search-media 60.170833 24.9375 5000 opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

;; Comments

(fact
  "Get comments" :integration
  (endpoints/get-comments "1196893517451621714_2987617812" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:text "foo"})])}))

(fact
  "Create comment" :integration
  (endpoints/create-comment "1196893517451621714_2987617812" "foo" opts)
  => (contains {:meta {:code 200}
                :data (contains {:text "foo"})}))

(fact
  "Delete comment" :integration
  (let [comment-id (-> (endpoints/create-comment "1196893517451621714_2987617812" "bar" opts)
                       (get-in [:data :id]))]
    (endpoints/delete-comment "1196893517451621714_2987617812" comment-id opts)
    => {:meta {:code 200}
        :data nil}))

;; Likes

(fact
  "Get likes" :integration
  (endpoints/get-likes "1196893517451621714_2987617812" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:username "cljtest2"})])}))

(fact
  "Set like" :integration
  (endpoints/set-like "1196893517451621714_2987617812" opts)
  => {:meta {:code 200}
      :data nil})

(fact
  "Remove like" :integration
  (endpoints/remove-like "1196893517451621714_2987617812" opts)
  => {:meta {:code 200}
      :data nil})

;; Tags

(fact
  "Get tag" :integration
  (endpoints/get-tag "nofilter" opts)
  => (just {:meta {:code 200}
            :data (just {:media_count pos?
                         :name        "nofilter"})}))

(fact
  "Get recently tagged media" :integration
  (endpoints/get-recently-tagged-media "nofilter" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Get recently tagged media (with options)" :integration
  (endpoints/get-recently-tagged-media "nofilter" {:count 1} opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Search tags" :integration
  (endpoints/search-tags "no" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:name "nofilter"})])}))

;; Locations

(fact
  "Get location" :integration
  (endpoints/get-location "736780008" opts)
  => (contains {:meta {:code 200}
                :data (contains {:name "Helsinki"})}))

(fact
  "Get recent media from location" :integration
  (endpoints/get-recent-media-from-location "736780008" opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Get recent media from location (with options)" :integration
  (endpoints/get-recent-media-from-location "736780008" {:max-id "1196893517451621714_2987617812"} opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:type "image"})])}))

(fact
  "Search location by coordinates" :integration
  (endpoints/search-locations {:lat 60.17 :lng 24.93 :distance 5000} opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:name "Tennispalatsi"})])}))

(fact
  "Search location by Facebook places id" :integration
  (endpoints/search-locations {:facebook-places-id "109595459060079"} opts)
  => (contains {:meta {:code 200}
                :data (contains [(contains {:name "Helsinki"})])}))
