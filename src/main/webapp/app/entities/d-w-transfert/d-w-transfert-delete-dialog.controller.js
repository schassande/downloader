(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWTransfertDeleteController',DWTransfertDeleteController);

    DWTransfertDeleteController.$inject = ['$uibModalInstance', 'entity', 'DWTransfert'];

    function DWTransfertDeleteController($uibModalInstance, entity, DWTransfert) {
        var vm = this;

        vm.dWTransfert = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DWTransfert.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
