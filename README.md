help-to-save-test-admin-frontend
================================
This frontend microservice provides various functions which facilitate testing for HTS. This microservice supports
three main functions:
- provides an interface to delete verified emails. This is useful in the ET environment to enable a clean
  slate when running tests
- creates curl requests to call HTS API's. This service handles all the calls to get a valid bearer token with the
  correct access type (user restricted or privileged) and configurable user info where appropriate.
- provides an oauth callback URL that can be used in API integration tests to provide a bearer token

Table of Contents
=================
* [About Help to Save](#about-help-to-save)
* [Running and Testing](#running-and-testing)
   * [Running](#running)
   * [Unit tests](#unit-tests)
* [Endpoints](#endpoints)
* [License](#license)


About Help to Save
==================
Please click [here](https://github.com/hmrc/help-to-save#about-help-to-save) for more information.

Running and Testing
===================

Running
-------
Run `sbt run` on the terminal to start the service. The service runs on port 7007 by default.

Unit tests
----------
Run `sbt test` on the terminal to run the unit tests.

Endpoints
=========
| Path                                                             | Method | Description  |
| -----------------------------------------------------------------| ------ | ------------ |
| /help-to-save-test-admin-frontend/                               | GET    | Shows a list of available functions through the UI |
| /help-to-save-test-admin-frontend/available-functions            | GET    | Shows a list of available functions through the UI |
| /help-to-save-test-admin-frontend/forbidden                      | GET    | Landing page for users who try to access the UI when their IP address has not been whitelisted and IP-whitelisting has been enabled |
| /help-to-save-test-admin-frontend/specify-emails-to-delete       | GET    | Shows a page where emails can be entered in for deletion |
| /help-to-save-test-admin-frontend/delete-emails                  | POST   | Deletes some given email from the backend mongo store |
| /help-to-save-test-admin-frontend/check-eligibility-page         | GET    | Shows a page to enter in details for a check eligibility API request |
| /help-to-save-test-admin-frontend/check-eligibility              | POST   | Gets a bearer token and generates a curl command for a check eligibility API request |
| /help-to-save-test-admin-frontend/get-create-account-page        | GET    | Shows a page to enter in details for a create account API request |
| /help-to-save-test-admin-frontend/create-account                 | POST   | Gets a bearer token and generates a curl command for a create account API request |
| /help-to-save-test-admin-frontend/get-account-page               | GET    | Shows a page to enter in details for a get account API request |
| /help-to-save-test-admin-frontend/account                        | GET    | Gets a bearer token and generates a curl command for a get account API request |
| /help-to-save-test-admin-frontend/curl-result                    | GET    | Shows a generated curl request |
| /help-to-save-test-admin-frontend/authorize-callback             | GET    | Internal endpoint to facilitate retrieving an API access token |
| /help-to-save-test-admin-frontend/authorize-callback-for-ittests | GET    | Endpoint to facilitate retrieving an API access token |

License
=======
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
