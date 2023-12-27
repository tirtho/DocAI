/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .controller('linkedInScoreCtrl', ['$scope', '$location', 'linkedInScoreSvc', function ($scope, $location, linkedInScoreSvc) {
        $scope.error = '';
        $scope.loadingMessage = '';
        $scope.linkedInScore = null;
        $scope.newLinkedInSearchString = '';
        $scope.item = null;
		$scope.itemScore = {};
		
		$scope.getUpdatedScore = function (item) {
			linkedInScoreSvc.getScore(item.keyPhrases)
			.success(function (result) {
				item.score = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		}
        $scope.populate = function () {
            linkedInScoreSvc.getItems().success(function (results) {
                $scope.linkedInScore = results;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
        $scope.add = function () {
			linkedInScoreSvc.postItem({
				'description': $scope.newLinkedInSearchString
			}).success(function (results) {
				$scope.linkedInScore = results;
			}).error(function (err) {
				$scope.error = err;
				$scope.loadingMessage = '';
			})
        };
    }]);
