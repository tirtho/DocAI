/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .controller('emailMessageReviewCtrl', ['$scope', '$location', 'emailMessageReviewSvc', function ($scope, $location, emailMessageReviewSvc) {
        $scope.error = '';
        $scope.loadingMessage = '';
        $scope.emailMessageReview = null;
        $scope.newEmailMessageReviewSearchString = '';
        $scope.item = null;
		$scope.itemScore = {};
		
		$scope.getUpdatedScore = function (item) {
			emailMessageReviewSvc.getScore(item.keyPhrases)
			.success(function (result) {
				item.score = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		}
        $scope.populate = function () {
            emailMessageReviewSvc.getItems().success(function (results) {
                $scope.emailMessageReview = results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
        $scope.add = function () {
			emailMessageReviewSvc.postItem({
				'description': $scope.newSearchString
			}).success(function (results) {
				$scope.emailMessageReview = results;
			}).error(function (err) {
				$scope.error = err;
				$scope.loadingMessage = '';
			})
        };
    }]);
