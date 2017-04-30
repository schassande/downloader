(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWHostAccountController', DWHostAccountController);

    DWHostAccountController.$inject = ['$scope', '$state', 'DWHostAccount'];

    function DWHostAccountController ($scope, $state, DWHostAccount) {
        var vm = this;
        
        vm.dWHostAccounts = [];

        loadAll();

        function loadAll() {
            DWHostAccount.query(function(result) {
                vm.dWHostAccounts = result;
            });
        }
    }
})();
