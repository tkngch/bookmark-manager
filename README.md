# Bookmark manager (Kotlin multiplatform)

![build](https://github.com/tkngch/bookmark-manager/workflows/build/badge.svg)

A web-application to manage bookmarks.

![screenshot](./screenshot.png)

A bookmark can have zero or more tags. By clicking tags on the side panel,
bookmarks with selected tags are displayed in the main pane.

To reduce clutter, tags are categorised into primary and secondary. Primary tags
are shown by default on the side pane, and secondary tags are shown only after
"Show More" is clicked.

The bookmarks are listed in the order of their relevance, such that the bookmark
with the highest relevance appears at the top of list. The relevance is inferred
with the browsing history, as implemented in `bookmark-scorer` library
([Link](https://github.com/tkngch/bookmark-scorer)).

## How to use

First, build the project.

```
gradlew installDist
```

Then execute the entry-point script.

```
build/install/bookmark-manager/bin/bookmark-manager
```

## How to add a user

To add a user, edit `src/jvmMain/resources/users.json`. This JSON file defines a
list of object, each of which is expected to have "username" and "password"
fields. Value of "password" field should be the hashed password. For example, if
the password is `password`, its hashed value is
`XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=`. See
`src/jvmMain/resources/dev_users.json` for an example.

Note that the hashed password can be obtained with the following shell command:

```
echo -n $password | openssl dgst -binary -sha256 | openssl base64
```

## To do

- Publish a distribution as part of github workflows.
- Tag recommendation, to predict which tags are more likely to be added to each bookmark.

## Won't do

- User management

  The assumption is that I am the only user of this application for the
  foreseeable future. As such, there is no plan to add "sign up" functionality
  within the application UI.

- Log-out feature.

  This application uses the basic-authentication, with which it is not
  straightforward to let users log-out. The current plan is to wait for `Ktor`
  to support an authentication method with a log-out feature: for example, the
  session authentication method.
