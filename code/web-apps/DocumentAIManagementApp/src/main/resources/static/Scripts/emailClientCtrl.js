/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .controller('emailClientCtrl', ['$scope', '$location', 'emailClientSvc', function ($scope, $location, emailClientSvc) {
        $scope.error = '';
        $scope.loadingMessage = '';
        $scope.newEmailClient = null;
        $scope.newEmailDemoUserName = '';
        $scope.newEmailDemoUserEmailAddress = '';
        $scope.newEmailSubject = '';
        $scope.newEmailBody = '';
        $scope.item = null;
		$scope.newEmailAutoInsuranceClaimForm = '';
		$scope.newEmailAutoInsuranceClaimImageFront = '';
		$scope.newEmailAutoInsuranceClaimImageBack = '';
		$scope.newEmailAutoInsuranceClaimVideo = '';
		$scope.newEmailCommercialInsuranceApplicationForm = '';
        $scope.attachments = [];

        $scope.add = function () {
			$scope.attachments.push('IC-handwritten-JeanGenet.pdf:' + $scope.newEmailAutoInsuranceClaimForm),
			$scope.attachments.push('2011LexusS350C-broken-front.png:' + $scope.newEmailAutoInsuranceClaimImageFront),
			$scope.attachments.push('2011LexusIS350C-broken-back.png:' + $scope.newEmailAutoInsuranceClaimImageBack),
			$scope.attachments.push('CarClaim480p.mp4:' + $scope.newEmailAutoInsuranceClaimVideo),
			$scope.attachments.push('ABCChemicals-NECommercialConstructionCompany-COMMERCIAL INSURANCE APPLICATION.pdf:' + $scope.newEmailCommercialInsuranceApplicationForm),

			emailClientSvc.postItem({
				'username': $scope.newEmailDemoUserName,
				'sender': $scope.newEmailDemoUserEmailAddress,
				'subject': $scope.newEmailSubject,
				'body': $scope.newEmailBody,
				'receiver': 'abc@x.com',
				'attachments': $scope.attachments
			}).success(function (results) {
				$scope.populate();
				$scope.loadingMessage = results;
			}).error(function (err) {
				$scope.attachments = [];
				$scope.error = err;
				$scope.loadingMessage = '';
			})
        };
        
        $scope.populate = function () {
	        $scope.error = '';
	        $scope.newEmailDemoUserName = '';
	        $scope.newEmailDemoUserEmailAddress = '';
	        $scope.loadingMessage = '';
	        $scope.newEmailClient = null;
	        $scope.newEmailSubject = '';
	        $scope.newEmailBody = '';
	        $scope.item = null;
			$scope.newEmailAutoInsuranceClaimForm = '';
			$scope.newEmailAutoInsuranceClaimImageFront = '';
			$scope.newEmailAutoInsuranceClaimImageBack = '';
			$scope.newEmailAutoInsuranceClaimVideo = '';
			$scope.newEmailCommercialInsuranceApplicationForm = '';
	        $scope.attachments = [];
        };

    }]);
