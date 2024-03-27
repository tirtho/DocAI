/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .factory('accountSvc', ['$http', function ($http) {
        return {
            getAccountProfile: function () {
				return $http.get('api/getAccountProfile/');
			}
        };
    }]);
