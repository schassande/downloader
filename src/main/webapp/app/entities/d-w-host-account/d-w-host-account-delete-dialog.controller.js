(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWHostAccountDeleteController',DWHostAccountDeleteController);

    DWHostAccountDeleteController.$inject = ['$uibModalInstance', 'entity', 'DWHostAccount'];

    function DWHostAccountDeleteController($uibModalInstance, entity, DWHostAccount) {
        var vm = this;

        vm.dWHostAccount = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DWHostAccount.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
