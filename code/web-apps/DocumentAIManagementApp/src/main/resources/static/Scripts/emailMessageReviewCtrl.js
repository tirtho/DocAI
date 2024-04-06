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
			
			document.getElementById("emailReviewBtn-".concat(item.id)).setAttribute("disabled", "disabled");
			document.getElementById("emailReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch fa-spin";
			
			emailMessageReviewSvc.getEmailMessageReviewSummary(item.id)
			.success(function (result) {
				item.emailMessageReviewSummary = result;

				document.getElementById("emailReviewBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("emailReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch";
				
				$scope.error = '';
				$scope.loadingMessage = '';

            }).error(function (err) {

				document.getElementById("emailReviewBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("emailReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch";
				
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
        $scope.delete = function (item) {
			
			document.getElementById("deleteBtn-".concat(item.id)).setAttribute("disabled", "disabled");
			document.getElementById("deleteIcn-".concat(item.id)).className = "fa fa-close fa-spin";
			
            emailMessageReviewSvc.deleteItem(item.id)
            .success(function (results) {

				document.getElementById("deleteBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("deleteIcn-".concat(item.id)).className = "fa fa-close";
				item = null;
                $scope.add();
                $scope.loadingMessage = results;
                $scope.error = '';
            }).error(function (err) {

				document.getElementById("deleteBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("deleteIcn-".concat(item.id)).className = "fa fa-close";
				
                $scope.error = err;
                $scope.loadingMessage = '';
				/*
				item = null;
                $scope.add();
                */
            })
        };
		$scope.getUpdatedAttachmentReviewSummary = function (item) {
			
			document.getElementById("attachmentReviewBtn-".concat(item.id)).setAttribute("disabled", "disabled");
			document.getElementById("attachmentReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch fa-spin";
			
			emailMessageReviewSvc.getAttachmentReviewSummary(item.id)
			.success(function (result) {
				item.attachmentReviewSummary = result;

				document.getElementById("attachmentReviewBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("attachmentReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch";
								
				$scope.error = '';
				$scope.loadingMessage = '';

            }).error(function (err) {

				document.getElementById("attachmentReviewBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("attachmentReviewIcn-".concat(item.id)).className = "fa fa-circle-o-notch";
								
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
		$scope.getUpdatedAttachmentExtracts = function (item) {
			
			document.getElementById("attachmentRefreshBtn-".concat(item.id)).setAttribute("disabled", "disabled");
			document.getElementById("attachmentRefreshIcn-".concat(item.id)).className = "fa fa-refresh fa-spin";
			
			emailMessageReviewSvc.getUpdatedAttachmentExtracts(item.id)
			.success(function (result) {
				item.extracts = result;

				document.getElementById("attachmentRefreshBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("attachmentRefreshIcn-".concat(item.id)).className = "fa fa-refresh";
								
				$scope.error = '';
				$scope.loadingMessage = '';

            }).error(function (err) {

				document.getElementById("attachmentRefreshBtn-".concat(item.id)).removeAttribute("disabled");
				document.getElementById("attachmentRefreshIcn-".concat(item.id)).className = "fa fa-refresh";
								
                $scope.error = err;
                $scope.loadingMessage = '';
            })
		};
        $scope.populate = function () {
            emailMessageReviewSvc.getItems().success(function (results) {
                $scope.emailMessageReview = results;
				
				$scope.error = '';
				$scope.loadingMessage = '';

            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        };
        $scope.add = function () {

			document.getElementById("emailSearchBtn").setAttribute("disabled", "disabled");
			document.getElementById("emailSearchIcn").className = "fa fa-spinner fa-spin";

			emailMessageReviewSvc.postItem({
				'description': $scope.newSearchString
			}).success(function (results) {
				$scope.emailMessageReview = results;

				document.getElementById("emailSearchBtn").removeAttribute("disabled");
				document.getElementById("emailSearchIcn").className = "fa fa-search";
				
				$scope.error = '';
				$scope.loadingMessage = '';

			}).error(function (err) {
				$scope.emailMessageReview = null;

				$scope.error = err;
				$scope.loadingMessage = '';

				document.getElementById("emailSearchBtn").removeAttribute("disabled");
				document.getElementById("emailSearchIcn").className = "fa fa-search";

			})
        };
    }]);
