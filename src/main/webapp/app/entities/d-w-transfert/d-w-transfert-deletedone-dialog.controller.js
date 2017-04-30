(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWTransfertDeleteDoneController',DWTransfertDeleteDoneController);

    DWTransfertDeleteDoneController.$inject = ['$uibModalInstance', 'DWTransfert'];

    function DWTransfertDeleteDoneController($uibModalInstance, DWTransfert) {
        var vm = this;

        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete () {
            DWTransfert.deleteDone(function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
