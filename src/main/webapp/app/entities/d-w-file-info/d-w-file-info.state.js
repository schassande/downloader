(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('d-w-file-info', {
            parent: 'entity',
            url: '/d-w-file-info',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWFileInfos'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-infos.html',
                    controller: 'DWFileInfoController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('d-w-file-info-detail', {
            parent: 'entity',
            url: '/d-w-file-info/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWFileInfo'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-info-detail.html',
                    controller: 'DWFileInfoDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'DWFileInfo', function($stateParams, DWFileInfo) {
                    return DWFileInfo.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'd-w-file-info',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('d-w-file-info-detail.edit', {
            parent: 'd-w-file-info-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-info-dialog.html',
                    controller: 'DWFileInfoDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWFileInfo', function(DWFileInfo) {
                            return DWFileInfo.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-file-info.new', {
            parent: 'd-w-file-info',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-info-dialog.html',
                    controller: 'DWFileInfoDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                fileInfoId: null,
                                path: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('d-w-file-info', null, { reload: true });
                }, function() {
                    $state.go('d-w-file-info');
                });
            }]
        })
        .state('d-w-file-info.edit', {
            parent: 'd-w-file-info',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-info-dialog.html',
                    controller: 'DWFileInfoDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWFileInfo', function(DWFileInfo) {
                            return DWFileInfo.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-file-info', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-file-info.delete', {
            parent: 'd-w-file-info',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-file-info/d-w-file-info-delete-dialog.html',
                    controller: 'DWFileInfoDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DWFileInfo', function(DWFileInfo) {
                            return DWFileInfo.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-file-info', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
