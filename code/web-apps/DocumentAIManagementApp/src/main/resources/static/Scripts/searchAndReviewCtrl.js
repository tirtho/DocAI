/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .controller('searchAndReviewCtrl', ['$scope', '$location', 'searchAndReviewSvc', function ($scope, $location, searchAndReviewSvc) {
        $scope.error = '';
        $scope.loadingMessage = '';
        $scope.searchAndReview = null;
        $scope.newSearchString = '';
        $scope.item = null;
		$scope.itemScore = {};
		
		$scope.getUpdatedScore = function (item) {
			searchAndReviewSvc.getScore(item.keyPhrases)
			.success(function (result) {
				item.score = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		}
        $scope.populate = function () {
            searchAndReviewSvc.getItems().success(function (results) {
                $scope.searchAndReview = results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
        $scope.add = function () {
			searchAndReviewSvc.postItem({
				'description': $scope.newSearchString
			}).success(function (results) {
				$scope.searchAndReview = results;
			}).error(function (err) {
				$scope.error = err;
				$scope.loadingMessage = '';
			})
        };
    }]);
