(function() {
    'use strict';
    angular
        .module('downloaderApp')
        .factory('FileBrowser', FileBrowser);

    FileBrowser.$inject = ['$resource'];

    function FileBrowser ($resource) {
        var resourceUrl =  'file-browser';

        return $resource(resourceUrl, {}, {
            'browse': { 
            	url : 'file-browser/browse',
            	method:'POST',
            	isArray: false,
            	cache: false
            },
	        'connect': { 
	        	url : 'file-browser/connect',
	        	method:'POST',
	        	isArray: false,
	        	cache: false
	        },
            'disconnect': { 
            	url : 'file-browser/disconnect',
            	method:'POST',
            	isArray: false,
            	cache: false
            }
        });
    }
})();
