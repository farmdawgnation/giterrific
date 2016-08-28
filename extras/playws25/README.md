# Play WS Driver for Play 2.5

This package contains a Giterific HTTP driver implementation that will be useful for folks using
Play 2.5. It uses the standard-issue Play WS implementation to make its HTTP requests with a
small compatibility shim for Giterrific.

## A note on testing

Because Technology™ this package is using a yet-unreleased version of
[scalatestplus-play](https://github.com/playframework/scalatestplus-play) to implement the tests.
You'll need to clone master from that repository and publish it locally for integration tests
to run.
