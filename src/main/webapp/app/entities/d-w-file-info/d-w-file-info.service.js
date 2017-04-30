(function() {
    'use strict';
    angular
        .module('downloaderApp')
        .factory('DWFileInfo', DWFileInfo);

    DWFileInfo.$inject = ['$resource'];

    function DWFileInfo ($resource) {
        var resourceUrl =  'api/d-w-file-infos/:id';

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
