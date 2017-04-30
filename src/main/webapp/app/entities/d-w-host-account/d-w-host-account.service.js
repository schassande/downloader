(function() {
    'use strict';
    angular
        .module('downloaderApp')
        .factory('DWHostAccount', DWHostAccount);

    DWHostAccount.$inject = ['$resource'];

    function DWHostAccount ($resource) {
        var resourceUrl =  'api/d-w-host-accounts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
