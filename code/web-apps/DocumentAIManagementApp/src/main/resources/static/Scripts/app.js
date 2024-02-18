/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp', ['ngRoute'])
    .config(['$routeProvider',  function ($routeProvider) {
        $routeProvider
        .when('/Home', {
            controller: 'homeCtrl',
            templateUrl: 'Views/Home.html',
        })
         .when('/EmailMessageReview', {
			controller: 'emailMessageReviewCtrl',
			templateUrl: 'Views/EmailMessageReview.html',
		})
         .when('/EmailClient', {
			controller: 'emailClientCtrl',
			templateUrl: 'Views/EmailClient.html',
		})
        .when('/SearchAndReview', {
            controller: 'searchAndReviewCtrl',
            templateUrl: 'Views/SearchAndReview.html',
        }).otherwise({redirectTo: '/Home'});
    }]);
