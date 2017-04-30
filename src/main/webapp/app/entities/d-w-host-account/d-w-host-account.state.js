(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('d-w-host-account', {
            parent: 'entity',
            url: '/d-w-host-account',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWHostAccounts'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-accounts.html',
                    controller: 'DWHostAccountController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('d-w-host-account-detail', {
            parent: 'entity',
            url: '/d-w-host-account/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWHostAccount'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-account-detail.html',
                    controller: 'DWHostAccountDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'DWHostAccount', function($stateParams, DWHostAccount) {
                    return DWHostAccount.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'd-w-host-account',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('d-w-host-account-detail.edit', {
            parent: 'd-w-host-account-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-account-dialog.html',
                    controller: 'DWHostAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWHostAccount', function(DWHostAccount) {
                            return DWHostAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-host-account.new', {
            parent: 'd-w-host-account',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-account-dialog.html',
                    controller: 'DWHostAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                host: null,
                                port: null,
                                userName: null,
                                password: null,
                                protocol: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('d-w-host-account', null, { reload: true });
                }, function() {
                    $state.go('d-w-host-account');
                });
            }]
        })
        .state('d-w-host-account.edit', {
            parent: 'd-w-host-account',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-account-dialog.html',
                    controller: 'DWHostAccountDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWHostAccount', function(DWHostAccount) {
                            return DWHostAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-host-account', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-host-account.delete', {
            parent: 'd-w-host-account',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-host-account/d-w-host-account-delete-dialog.html',
                    controller: 'DWHostAccountDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DWHostAccount', function(DWHostAccount) {
                            return DWHostAccount.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-host-account', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
