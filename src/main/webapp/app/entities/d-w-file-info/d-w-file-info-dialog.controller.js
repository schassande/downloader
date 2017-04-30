(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWFileInfoDialogController', DWFileInfoDialogController);

    DWFileInfoDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DWFileInfo', 'DwHostAccount'];

    function DWFileInfoDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, DWFileInfo, DwHostAccount) {
        var vm = this;

        vm.dWFileInfo = entity;
        vm.clear = clear;
        vm.save = save;
        vm.dwhostaccounts = DwHostAccount.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.dWFileInfo.id !== null) {
                DWFileInfo.update(vm.dWFileInfo, onSaveSuccess, onSaveError);
            } else {
                DWFileInfo.save(vm.dWFileInfo, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('downloaderApp:dWFileInfoUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
