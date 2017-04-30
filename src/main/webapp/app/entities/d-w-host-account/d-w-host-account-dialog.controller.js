(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWHostAccountDialogController', DWHostAccountDialogController);

    DWHostAccountDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DWHostAccount'];

    function DWHostAccountDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, DWHostAccount) {
        var vm = this;

        vm.dWHostAccount = entity;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        vm.clear = clear;
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        
        vm.save = save;
        function save () {
            vm.isSaving = true;
            if (vm.dWHostAccount.id !== null) {
                DWHostAccount.update(vm.dWHostAccount, onSaveSuccess, onSaveError);
            } else {
                DWHostAccount.save(vm.dWHostAccount, onSaveSuccess, onSaveError);
            }
        }
        function onSaveSuccess (result) {
            $scope.$emit('downloaderApp:dWHostAccountUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
