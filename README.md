# Bookmark manager (Kotlin multiplatform)

A web-application to manage bookmarks.

## Tag

A bookmark can have zero, one, or more tags. By clicking tags on the side panel,
bookmarks with selected tags are displayed in the main pane.

To reduce clutter, tags are categorised into primary and secondary. Primary tags
are shown by default on the side pane, and secondary tags are shown only after
"Show More" is clicked.

## TODO

- Make "New Addition" tag to be selected by default.
- Drop the seeding for development.
- Tests for kotlin/js parts.

## Planned features

- Publication of binary package.
- Scoring feature, to indicate how like a bookmark is predicted to be visited.

## Not in the plan

- Log-out feature.

  This application uses the basic-authentication, with which it is not
  straightforward to let users log-out. The current plan is to wait for `Ktor`
  to support an authentication method with a log-out feature: for example, the
  session authentication method.
