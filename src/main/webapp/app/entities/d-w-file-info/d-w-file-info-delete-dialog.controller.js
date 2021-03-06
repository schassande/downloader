(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWFileInfoDeleteController',DWFileInfoDeleteController);

    DWFileInfoDeleteController.$inject = ['$uibModalInstance', 'entity', 'DWFileInfo'];

    function DWFileInfoDeleteController($uibModalInstance, entity, DWFileInfo) {
        var vm = this;

        vm.dWFileInfo = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DWFileInfo.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
