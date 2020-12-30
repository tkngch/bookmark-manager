# Bookmark manager (Kotlin multiplatform)

![build](https://github.com/tkngch/bookmark-manager/workflows/build/badge.svg)

A web-application to manage bookmarks.

![screenshot](./screenshot.png)

## Main features

### Tagging

A bookmark can have zero or more tags. By clicking tags on the side panel,
bookmarks with selected tags are displayed in the main pane.

To reduce clutter, tags are categorised into primary and secondary. Primary tags
are shown by default on the side pane, and secondary tags are shown only after
"Show More" is clicked.

### Ordering by relevance

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

This application is build with Java 11.

### Add a user

To add a user, create `$XDG_DATA_HOME/bookmark-manager/user.json`. In case where
`XDG_DATA_HOME` is not set, create
`$HOME/.local/share/bookmark-manager/user.json`. This json file defines a list
of users with their hashed password. For example, the content can be

```
[
    {
        "username": "test-user",
        "password": "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg="
    }
]
```

in which case one user `test-user` is recognised by the application. Each entry
is expected to have "username" and "password" fields. Value of "password" field
should be the hashed password. The hashed password can be obtained with the
following shell command:

```
echo -n $password | openssl dgst -binary -sha256 | openssl base64
```

For example,

```
echo -n 'password' | openssl dgst -binary -sha256 | openssl base64
```

gives us

```
XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=
```

which appears in the above example.

For more example of `users.json` content, see
`src/jvmMain/resources/dev_users.json`. This `dev_users.json` defines users on
the development server.

## To do

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
