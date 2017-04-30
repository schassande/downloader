(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWFileInfoDetailController', DWFileInfoDetailController);

    DWFileInfoDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'DWFileInfo', 'DwHostAccount'];

    function DWFileInfoDetailController($scope, $rootScope, $stateParams, previousState, entity, DWFileInfo, DwHostAccount) {
        var vm = this;

        vm.dWFileInfo = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('downloaderApp:dWFileInfoUpdate', function(event, result) {
            vm.dWFileInfo = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
