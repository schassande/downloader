(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWTransfertController', DWTransfertController);

    DWTransfertController.$inject = ['$scope', '$state', 'DWTransfert'];

    function DWTransfertController ($scope, $state, DWTransfert) {
        var vm = this;
        
        vm.dWTransferts = [];

        loadAll();

        function loadAll() {
            DWTransfert.query(function(result) {
                vm.dWTransferts = result;
            });
        }
        
        vm.getFileName = function(path)
        {
        	var idx = path.lastIndexOf('/');
        	if (idx >= 0) {
        		return path.substring(idx+1);
        	} else {
        		return path;
        	}
        }
        vm.getClassFromStatus = function(status) {
        	switch(status){
        	case 'CREATED': return 'info'; 
        	case 'DOING'  : return 'warning'; 
        	case 'DONE'   : return 'success'; 
        	case 'ERROR'  : return 'danger'; 
        	default: return '';
        	}
        }
        
        
        vm.run = function run (id) 
        {
            if (id != null) {
                var t = { id : id };
                DWTransfert.run(t);
            }
        }
        
        vm.getPercent = function(transfert) {
        	return (transfert.downloaded * 100) / transfert.fileSize; 
        }
    }
})();
