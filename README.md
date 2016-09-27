# clj-instagram [![Build Status](https://travis-ci.org/immoh/clj-instagram.svg?branch=master)](https://travis-ci.org/immoh/clj-instagram)

Clojure client for Instagram API.

For more details about Instagram API refer to official documentation
at https://www.instagram.com/developer/.


## Installation

Add to your Leiningen/Boot dependencies:

```clj
[clj-instagram "0.2.0"]
```

## Quick Start

```clj
(ns myapp
  (:require [clj-instagram.oauth :as oauth]
            [clj-instagram.endpoints :as endpoints]))

;; Register you application at https://www.instagram.com/developer/ to obtain client id and client secret.
(def client-id "1d3c2f3e457bda7608759ed8eaf4a3d1")
(def client-secret "cadef5e0e7241fb847c6901bf043876e")
(def redirect-uri "http://myapp.com/oauth/callback/")

;; Create authorization URL and direct user to this URL to authenticate and authorize your app.
(oauth/authorization-url client-id redirect-uri)
=> "https://api.instagram.com/oauth/authorize/?client_id=1d3c2f3e457bda7608759ed8eaf4a3d1&redirect_uri=http%3A%2F%2Fmyapp.com%2Foauth%2Fcallback%2F&response_type=code"

;; After successful authentication and authorization Instagram will redirect user to the given URL 
;; appended with a code, e.g.:
;; http://myapp.com/oauth/callback/?code=7e0a6bf7db588f4bca7042113026e2d8

;; Exchange this code for an access token
(oauth/request-access-token client-id client-secret redirect-uri "7e0a6bf7db588f4bca7042113026e2d8")
=> {:access_token "334239322.1d3c2f.ecb836549a83694bf7d783915d2c4d3a",
    :user {:username "immoh",
           :bio "",
           :website "",
           :profile_picture "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13534243_201474086916849_88831616_a.jpg",
           :full_name "Immo Heikkinen",
           :id "334239322"}}

;; Start making requests using the access token (and client secret for request signing)
(endpoints/get-user {:access-token "334239322.1d3c2f.ecb836549a83694bf7d783915d2c4d3a" 
                     :client-secret client-secret})
=> {:meta {:code 200},
    :data {:username "immoh",
           :bio "",
           :website "",
           :profile_picture "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13534243_201474086916849_88831616_a.jpg",
           :full_name "Immo Heikkinen",
           :counts {:media 77, :followed_by 74, :follows 237},
           :id "334239322"}}
```

## Documentation

Detailed API documentation can be found at https://immoh.github.io/clj-instagram/.


### Responses

Typically the client will return a Clojure data structure representing JSON
response from Instagram. 

The response envelope looks following:

```clj
{:meta {:code 200}
 :data ...}
```

For bad requests (HTTP status code 400) the client will not throw
exception but return a Clojure data structure for the error response.
The user of the client should investigate the value of `:code` key to
find out whether the request was successful or not.

For example, using an invalid access token will result in the following
return value:

```clj
{:meta {:code 400, 
        :error_type "OAuthAccessTokenException", 
        :error_message "The access_token provided is invalid."}}
```

All other exceptional status codes, parsing errors, connection errors etc.
will result in an exception.


### HTTP Client Options

This library uses [clj-http](https://github.com/dakrone/clj-http) as 
HTTP client. All functions that make HTTP requests also allow passing 
options to the HTTP client.

All functions in `clj-instagram.endpoints` take a mandatory options map 
as last parameter. You can pass options to the HTTP client using
`:http-client-opts` key:

```clj
(require '[clj-instagram.endpoints :as endpoints])

(endpoints/get-user {:access-token "334239322.1d3c2f.ecb836549a83694bf7d783915d2c4d3a"
                     :client-secret "cadef5e0e7241fb847c6901bf043876e"
                     :http-client-opts {:socket-timeout 1000 :conn-timeout 1000}})
=> {:meta {:code 200},
    :data {:username "immoh",
           :bio "",
           :website "",
           :profile_picture "https://scontent.cdninstagram.com/t51.2885-19/s150x150/13534243_201474086916849_88831616_a.jpg",
           :full_name "Immo Heikkinen",
           :counts {:media 77, :followed_by 74, :follows 237},
           :id "334239322"}}
```

Other functions that call the Instagram API (namespaces 
`clj-instagram.oauth` and `clj-instagram.subscriptions`) accept a map of 
HTTP client options as the last parameter (optional):

```clj
(require '[clj-instagram.subscriptions :as subscriptions])

(subscriptions/list "1d3c2f3e457bda7608759ed8eaf4a3d1" 
                    "cadef5e0e7241fb847c6901bf043876e" 
                    {:socket-timeout 1000 :conn-timeout 1000})
=> {:meta {:code 200}, :data []}
```

Refer to clj-http documentation for all available HTTP client options.

### Using Another HTTP Client

If you wish to use other HTTP client than clj-http, `clj-instagram.requests` namespace contains pure functions for
creating and manipulating maps that represent HTTP request. These maps are compatible with clj-http and should be usable
with any HTTP client library with minor modifications.

Here's an example how to create request for getting current user information, adding access token and signing request
with client secret:

```clj
(require '[clj-instagram.requests :as requests])

(-> (requests/get-user)
    (requests/authenticate "334239322.1d3c2f.ecb836549a83694bf7d783915d2c4d3a")
    (requests/sign "cadef5e0e7241fb847c6901bf043876e"))
=> {:method :get,
    :url "https://api.instagram.com/v1/users/self",
    :query-params {:access_token "334239322.1d3c2f.ecb836549a83694bf7d783915d2c4d3a",
                   :sig "51fa4e1775a37b493dfd1bcee5c3293c51835f3d3a2e7438df700cf50f6f88de"}}
```

### OAuth

The Instagram API requires authentication - specifically requests made on behalf of a user. 
Authenticated requests require an access token. These tokens are unique to a user 
and should be stored securely. Access tokens may expire at any time in the future.

In order to receive an access token, you must do the following:

1. Create an authorization url using [authorization-url](https://immoh.github.io/clj-instagram/clj-instagram.oauth.html#var-authorization-url)
function and direct the user to this url.
  * If the user is not logged in, they will be asked to log in.
  * The user will be asked if they would like to grant your application access to her Instagram data.

2. The server will redirect user to an URL of your choice and pass a code as a query parameter.

3. Exchange the code for an access token using 
[request-access-token](https://immoh.github.io/clj-instagram/clj-instagram.oauth.html#var-request-access-token) function

See https://www.instagram.com/developer/authentication/ for more details about authentication.


### Endpoints

Endpoint calls are made on behalf of user and they  require a valid 
access token which can be obtained as described above. 
By default all requests are signed with client secret.
Signing can be disabled but this is not recommended.

The last parameter of every endpoint call function is a map with the 
following possible keys:

* `:access-token`     - a valid access token
* `:client-secret`    - client secret for encoding the request (optional)
* `:sign-request?`    - sign requests (optional, default: true)
* `:http-client-opts` - map of HTTP client options (optional)

See https://www.instagram.com/developer/endpoints/ for more details about different endpoints.

#### Users

* [get-user](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-user)
* [get-recent-media-by-user](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-recent-media-by-user)
* [get-recent-media-by-me](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-recent-media-by-me)
* [get-liked-media](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-liked-media)
* [search-users](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-search-users)

#### Relationships

* [get-follows](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-follows)
* [get-followed-by](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-followed-by)
* [get-requested-by](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-requested-by)
* [get-relationship](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-relationship)
* [modify-relationship](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-modify-relationship)

#### Media

* [get-media-by-id](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-media-by-id)
* [get-media-by-shortcode](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-media-by-shortcode)
* [search-media](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-search-media)

#### Comments

* [get-comments](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-comments)
* [create-comment](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-create-comment)
* [delete-comment](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-delete-comment)

#### Likes

* [get-likes](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-likes)
* [set-like](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-set-like)
* [remove-like](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-remove-like)

#### Tags

* [get-tag](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-tag)
* [get-recently-tagged-media](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-recently-tagged-media)
* [search-tags](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-search-tags)

#### Locations

* [get-location](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-location)
* [get-recent-media-from-location](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-get-recent-media-from-location)
* [search-locations](https://immoh.github.io/clj-instagram/clj-instagram.endpoints.html#var-search-locations)

### Subscriptions

* [create](https://immoh.github.io/clj-instagram/clj-instagram.subscriptions.html#var-create)
* [list](https://immoh.github.io/clj-instagram/clj-instagram.subscriptions.html#var-list)
* [delete](https://immoh.github.io/clj-instagram/clj-instagram.subscriptions.html#var-delete)

See https://www.instagram.com/developer/subscriptions/ for more details about subscriptions.

## Development

Pull requests welcome.

This project uses [Midje](https://github.com/marick/Midje) for testing. There is a bunch of facts that are tagged
with `:integration` and are run against live Instagram API. These facts require valid access token and client secret
that are read from environment variables and won't be found in the codebase. I will not expose these to public and
these tests are not run by CI server. In order to skip these tests when running tests locally, please use the
following Midje filter:

```
lein midje :filter -integration
```

## License

Copyright © 2016 Immo Heikkinen

Distributed under the Eclipse Public License, the same as Clojure.
