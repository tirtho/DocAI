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
         .when('/LinkedInScore', {
			controller: 'linkedInScoreCtrl',
			templateUrl: 'Views/LinkedInScore.html',
		})
        .when('/SearchAndReview', {
            controller: 'searchAndReviewCtrl',
            templateUrl: 'Views/SearchAndReview.html',
        }).otherwise({redirectTo: '/Home'});
    }]);
