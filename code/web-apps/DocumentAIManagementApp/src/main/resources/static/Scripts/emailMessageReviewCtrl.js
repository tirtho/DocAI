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
        $scope.attachment = null;
		$scope.itemEmailMessageReviewSummary = {};
		$scope.itemAttachmentReviewSummary = null;
		$scope.itemAttachmentExtract = null;

		$scope.getUpdatedEmailMessageReviewSummary = function (item) {
			emailMessageReviewSvc.getEmailMessageReviewSummary(item.id)
			.success(function (result) {
				item.emailMessageReviewSummary = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
        $scope.delete = function (id) {
            emailMessageReviewSvc.deleteItem(id)
            .success(function (results) {
                $scope.populate();
                $scope.loadingMessage = results;
                $scope.error = '';
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
		$scope.getUpdatedAttachmentReviewSummary = function (item) {
			emailMessageReviewSvc.getAttachmentReviewSummary(item.id)
			.success(function (result) {
				item.attachmentReviewSummary = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
		$scope.getUpdatedAttachmentExtracts = function (item) {
			emailMessageReviewSvc.getUpdatedAttachmentExtracts(item.id)
			.success(function (result) {
				item.extracts = result;
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
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
