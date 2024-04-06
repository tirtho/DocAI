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
		$scope.index = 0;
		
		$scope.getUpdatedScore = function (item) {
			
			document.getElementById("scoreBtn-".concat(item.searchId)).setAttribute("disabled", "disabled");
			document.getElementById("scoreIcn-".concat(item.searchId)).className = "fa fa-circle-o-notch fa-spin";
						
			searchAndReviewSvc.getScore(item.keyPhrases)
			.success(function (result) {
				item.score = result;

				document.getElementById("scoreBtn-".concat(item.searchId)).removeAttribute("disabled");
				document.getElementById("scoreIcn-".concat(item.searchId)).className = "fa fa-circle-o-notch";

                $scope.error = '';
                $scope.loadingMessage = '';
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';

				document.getElementById("scoreBtn-".concat(item.searchId)).removeAttribute("disabled");
				document.getElementById("scoreIcn-".concat(item.searchId)).className = "fa fa-circle-o-notch";

            })
		}
        $scope.populate = function () {
            searchAndReviewSvc.getItems().success(function (results) {
                $scope.searchAndReview = results;
				
				$scope.error = '';
				$scope.loadingMessage = '';

            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
        $scope.add = function () {
			
			document.getElementById("bingSearchBtn").setAttribute("disabled", "disabled");
			document.getElementById("bingSearchIcn").className = "fa fa-spinner fa-spin";
			
			searchAndReviewSvc.postItem({
				'description': $scope.newSearchString
			}).success(function (results) {
				$scope.searchAndReview = results;
				
				document.getElementById("bingSearchBtn").removeAttribute("disabled");
				document.getElementById("bingSearchIcn").className = "fa fa-search";
				
				$scope.error = '';
				$scope.loadingMessage = '';

			}).error(function (err) {
				$scope.error = err;
				$scope.loadingMessage = '';
				
				document.getElementById("bingSearchBtn").removeAttribute("disabled");
				document.getElementById("bingSearchIcn").className = "fa fa-search";
				
			})
        };
    }]);
