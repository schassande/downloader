(function() {
    'use strict';
    angular
        .module('downloaderApp')
        .factory('DWTransfert', DWTransfert);

    DWTransfert.$inject = ['$resource', 'DateUtils'];

    function DWTransfert ($resource, DateUtils) {
        var resourceUrl =  'api/d-w-transferts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.start = DateUtils.convertDateTimeFromServer(data.start);
                    }
                    return data;
                }
            },
            'update': { method:'PUT'},
            'run': { 
            	url : 'api/d-w-transferts-run/:id',
            	method:'GET',
            	isArray: false,
            	cache: false
            },
            'maxRank': { 
            	url : 'api/d-w-transferts-max-rank',
            	method:'GET',
            	isArray: false,
            	cache: false
            },
            'createMultiple': {
            	url : 'api/multiple-d-w-transferts',
            	method:'POST',
            	isArray: false,
            	cache: false
            },
            'deleteDone': {
            	url : 'api/deleteTransfertsByStatus/DONE',
            	method:'GET',
            	isArray: false,
            	cache: false
            }
        });
    }
})();
