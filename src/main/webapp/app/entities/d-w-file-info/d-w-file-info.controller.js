(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWFileInfoController', DWFileInfoController);

    DWFileInfoController.$inject = ['$scope', '$state', 'DWFileInfo'];

    function DWFileInfoController ($scope, $state, DWFileInfo) {
        var vm = this;
        
        vm.dWFileInfos = [];

        loadAll();

        function loadAll() {
            DWFileInfo.query(function(result) {
                vm.dWFileInfos = result;
            });
        }
    }
})();
