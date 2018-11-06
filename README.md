# Helmsman
#### It's time to take the helm and navigate your web server!

## Dependency
[![Clojars Project](https://img.shields.io/clojars/v/io.doane/helmsman.svg)](https://clojars.org/io.doane/helmsman)

```clojure
[io.doane/helmsman "1.1.0-SNAPSHOT"]
```

## What is Helmsman?
Helmsman is a Ring compliant HTTP routing tool that leverages routes by keeping
them data, using that data to manage routing, and providing that data in the
request for whatever the developer decides to use it for. For example, you can mark
routes with meta-data to tie them together to dynamically create things like
breadcrumbs or navigation links. Helmsman's URI engine also provides
functionality to calculate relative URIs, eliminating the need to rely on the
hostname for linking.

## Stanzas
Helmsman can define routes several different ways but, there are three different
kinds of mechanisms that Helmsman uses to construct them. You have **routes**,
**contexts**, and **middlewares**. All of these contructs have syntax which are
dubbed **Stanzas** where are merely vectors.

### Contexts
A context is a group that serve certain kinds of web requests of your choosing.
These are not routes but, have the benefit being able to clump routes together
for the purposes of common paths or for common middleware. Providing paths is
not a requirement for contexts where it was while Helmsman was in alpha.

Where ```...``` represents additional, nested routes, middleware, or contexts,
the following would be considered a **context**.
```clojure
[...]
["user/about" ...]
[:context ...]
[:context "my-info" ..,]
```

### Routes
A route is a HTTP endpoint but, has all of the same properties as a context
which allows routes to be nested inside of each other based on common paths.

Routes always describe an HTTP method, but optionally a path.
```clojure
[:get some-handler-fn]
[:get "profile" some-handler-fn]
```

Routes can also encompass several HTTP method at once by providing a set
of HTTP methods. The ```:any``` keyword matches all HTTP methods.
```clojure
[#{:get :post :put} some-handler-fn]
[:any some-handler-fn]
```

Routes are also contexts which can have routes under them.
```clojure
[:get home-page-handler
 [:get "about-us" about-us-handler]
 [:get "contact-us" contact-us-handler]]
```

### Middleware
Middleware are functions that sit between the the request coming in and the
handler. These are functions that take in a handler plus any number of arguements
which produce a function which take a single argument, a Ring request, and does
whatever it wants with it. Helmsman puts all of this together into a prepared
function call when a web request that matches a particular path comes in.
Middleware Stanzas describe the function and the arguments following the
handler function which gets provided when the routes are compiled. An example
of this might look as follows:

```clojure
[:get home-page-handler
 [[authorization-fn :require-login]
  ["my-info"
   [:get my-info-handler]
   [:post update-my-info]]
  [[require-roles #{:admin}]
   [:get "admin" admin-handler
   ...]]]]
```

### Metadata
Just like regular Clojure metadata, this information is not part of the route
but is there to describe the route. This information will be available when
a request comes in and gets attached to the formed Helmsman route in the
compiled routing set which is available to the request. There is a single
middleware key that's special which is the ```:id``` key which is supposed to
uniquely identify a route.

```clojure
["my-routes"
 ^{:id :about-my-routes
   :nav-lists #{:my-navs}
 [:get "about" handler-fn]
 ^{:id :save-my-routes
   :nav-lists #{:my-navs}}
 [:post "save" save-handler-fn]]
```

In this case, two routes have unique IDs so requests can be referenced and used
for linking using relative URIs as well as a ```:nav-lists``` identifier that
could be used for dynamically building navigation lists by getting all routes
based on the provided metadata. That's it. How this information is used is
completely up to the developer implementing it.

## Requests
Once a request has been matched up with a route, it gets dispathed with all
appropriate middleware and eventually the handler if the middlewares allow. How
middleware and handlers behave is exactly the same as [any other ring-based web
application](https://github.com/ring-clojure/ring/wiki/Concepts#requests) but,
provides some extra data pertaining to the available routes in the Ring
request. Helmsman adds a map to the request with the key ```:helmsman```. This
map contains information pertaining to the route that matched to this request,
the set of all the routes, as well as the broken-apart path that came in from
the request.

The map currently contains the following keys:

- ```:request-path``` is a vector that describes the URI of the request. So,
  a browser with a URI of ```admin/roles/1234/update``` would get turned into
  a vector that looks like ```["admin" "roles" "1234" "update"]``` but, if there
  is a trailing slash, we capture it for the purposes of properly creating
  relative URIs: ```admin/roles/``` => ```["admin" "roles" ""]``` but, they're
  stripped out when matching routes.
- ```:request-signature``` is a vector of characters that represents the first
  character of each URI segment or an identifier for a keyword in that position.
  This does not uniquely identify a route but offers a quick way of finding
  possible matches. This isn't currently used for anything.
- ```:routing-set``` is a set of all of the compiled routes. This can be used
  for whatever purpose a developer sees fit but, is mainly used for navigation.
- ```:current-route``` is the item from ```:routing-set``` that matches the
  current request.
- ```:request-path-params``` is a map that matches any keyword URI segments in
  the currently matched route's path.

## Navigation
One of the best parts about having route information handy at the time a request
is made is that it enables the ability to use that route data to generate relative links
for you based on the compiled routes.

So lets say you have some routes:
```clojure
[^{:id :home}
 [:get home-page-handler]
 ["user"
  ^{:id :profile}
  [:get "profile" profile-handler]
  ^{:id :reset-password}
  [:get "reset-password" reset-password-handler]]]
```

So long as you have the request, you can use the following to create
relative URI paths:
```clojure
(require ['helmsman.navigation :refer ['get-relative-path 'assemble-relative-uri]])
(get-relative-path request :home)
(get-relative-path request :profile)
(get-relative-path request :reset-password)
```

Depending on ```:request-path```, Helmsman automatically generates a relative
URI to the route that has the provided id. There are other functions within
```helmsman.navigation``` and ```helmsman.request``` to get routes in different
ways. ```helmsman.uri``` provides functionality for creating string URIs to be
used. The ```helmsman.navigation/assemble-relative-uri``` function is used to
contstruct a URL that may have keywords in it, which can be provided to the
function as substitutions.

## License

### Copyright and credits
 - VLACS© <jdoane@vlacs.org> 2014
 - Jon Doane <jrdoane@gmail.com> 2014-2018

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
