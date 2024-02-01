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
            getEmailMessageReviewSummary: function (id) {
				return $http.get('api/emailMessageReview/' + id);
            },
            deleteItem: function (id) {
                return $http({
                    method: 'DELETE',
                    url: 'api/emailMessageReview/' + id
                });
			},
            getUpdatedAttachmentExtracts: function (id) {
				return $http.get('api/attachmentExtracts/' + id);
			},
            getAttachmentReviewSummary: function (id) {
				return $http.get('api/attachmentReview/' + id);
			}
        };
    }]);
