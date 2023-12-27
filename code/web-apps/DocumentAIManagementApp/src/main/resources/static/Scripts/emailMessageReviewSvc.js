/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

'use strict';
angular.module('documentAIManagementApp')
    .factory('emailMessageReviewSvc', ['$http', function ($http) {
        return {
            postItem: function (item) {
                return $http.post('api/emailMessageReview/', item);
            },
            getScore: function (keyPhrases) {
				return $http.get('api/emailMessageReview/' + keyPhrases);
			}
        };
    }]);
