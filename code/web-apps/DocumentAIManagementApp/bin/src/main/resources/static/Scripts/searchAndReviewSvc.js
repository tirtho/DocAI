/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .factory('searchAndReviewSvc', ['$http', function ($http) {
        return {
            postItem: function (item) {
                return $http.post('api/searchAndReview/', item);
            },
            getScore: function (keyPhrases) {
				return $http.get('api/searchAndReview/' + keyPhrases);
			}
        };
    }]);
