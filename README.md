# help-to-save-test-admin-frontend

[![Build Status](https://travis-ci.org/hmrc/help-to-save-test-admin-frontend.svg)](https://travis-ci.org/hmrc/help-to-save-test-admin-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/help-to-save-test-admin-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/help-to-save-test-admin-frontend/_latestVersion)

This frontend microservice is to help set up ET so that its ready to run the Integration Tests. We can remove specified 
emails from the verifiedEmail collection in the email-verification database. This makes it possible to know the emails 
we use for testing in ET are not currently verified prior to running the tests which involve having email addresses 
verified.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
