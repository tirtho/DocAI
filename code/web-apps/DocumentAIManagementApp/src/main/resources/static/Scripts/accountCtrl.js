/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .controller('accountCtrl', ['$scope', '$location', 'accountSvc', function ($scope, $location, accountSvc) {
		$scope.loggedInUserProfile = null;
        $scope.error = '';
        $scope.loadingMessage = '';
		
        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        }
        $scope.getAccountProfile = function () {
			accountSvc.getAccountProfile()
			.success(function (result) {
                $scope.loggedInUserProfile = result;
                $scope.error = '';
                $scope.loadingMessage = '';
           }).error(function (err) {
                $scope.loggedInUserProfile = null;
                $scope.error = err;
                $scope.loadingMessage = '';
           })
		}
        $scope.populate = function () {
            accountSvc.getAccountProfile()
            .success(function (results) {
                $scope.loggedInUserProfile = results;
                $scope.error = '';
                $scope.loadingMessage = '';
            }).error(function (err) {
                $scope.error = err;
                $scope.loadingMessage = '';
            })
        }
    }]);
