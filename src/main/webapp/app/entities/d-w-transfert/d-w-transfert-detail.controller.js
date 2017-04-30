(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWTransfertDetailController', DWTransfertDetailController);

    DWTransfertDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DWTransfert', 'DWHostAccount'];

    function DWTransfertDetailController($scope, $rootScope, $stateParams, previousState, entity, DWTransfert, DWHostAccount) {
        var vm = this;

        vm.dWTransfert = entity;
        vm.previousState = previousState.name;

        vm.formatSeconds = function(seconds)
        {
        	if (seconds == 0) {
        		return "00:00:00";
        	}
        	if (!seconds) {
        		return "-";
        	}
            var date = new Date(1970,0,1);
            date.setSeconds(seconds);
            return date.toTimeString().replace(/.*(\d{2}:\d{2}:\d{2}).*/, "$1") +" (" + seconds +"s)";
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

        var unsubscribe = $rootScope.$on('downloaderApp:dWTransfertUpdate', function(event, result) {
            vm.dWTransfert = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
