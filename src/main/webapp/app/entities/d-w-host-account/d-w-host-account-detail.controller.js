(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWHostAccountDetailController', DWHostAccountDetailController);

    DWHostAccountDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DWHostAccount', 'FileBrowser'];

    function DWHostAccountDetailController($scope, $rootScope, $stateParams, previousState, entity, DWHostAccount, FileBrowser) {
        var vm = this;

        vm.dWHostAccount = entity;
        vm.previousState = previousState.name;
        vm.path = vm.dWHostAccount.defaultPath;
        vm.sortOrder = -1;
        vm.sortType     = 'filename'; // set the default sort type
        vm.sortReverse  = false;  // set the default sort order
        vm.searchFish   = '';     // set the default search/filter term
        
        function onBrowseResponse(browseResponse) {
        	vm.path = browseResponse.path;
        	vm.files = browseResponse.files.filter(function(file) {return file.filename == '..' || !file.filename.startsWith('.');});
        }
        
        function onBrowseFail() {
        	console.log('onBrowseFail!')
        }
        
        function onBrowse() {
        	var path = vm.path;
        	var req = {
        		path : vm.path,
        		account : vm.dWHostAccount,
        		directoryOnly : false
        	}
        	FileBrowser.browse(req, onBrowseResponse, onBrowseFail);
        }
        vm.onBrowse = onBrowse;

        function toOctets(size) {
        	if (size > (1024*1024*1024)) {
        		return (size / (1024*1024*1024)).toFixed(2) + ' Go';
        	} else if (size > (1024*1024)) {
        		return (size / (1024*1024)).toFixed(2) + ' Mo';
        	} else if (size > 1024) {
        		return (size / 1024).toFixed(2) + ' ko';
        	} else {
        		return size + ' o'
        	}
        }
        vm.toOctets = toOctets;
        
        function select(file){
        	if (file.dir) {
        		vm.path = vm.path + vm.dWHostAccount.pathSeparator + file.filename;
        		onBrowse();
        	}
        }
        vm.select = select;
        
        vm.connected = 0;
        function manageConnection(){
        	if (vm.connected) {
        		vm.connected = 0;
            	FileBrowser.disconnect(vm.dWHostAccount);
        	} else {
        		vm.connected = 1;
            	FileBrowser.connect(vm.dWHostAccount);
        	}
        }
        vm.manageConnection = manageConnection;

        function toDate(fileTime) {
        	var d = new Date(fileTime);
        	return d.toString();
        }
        vm.toDate = toDate;
        var unsubscribe = $rootScope.$on('downloaderApp:dWHostAccountUpdate', function(event, result) {
            vm.dWHostAccount = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
