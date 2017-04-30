(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('d-w-transfert', {
            parent: 'entity',
            url: '/d-w-transfert',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWTransferts'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-transfert/d-w-transferts.html',
                    controller: 'DWTransfertController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('d-w-transfert-detail', {
            parent: 'entity',
            url: '/d-w-transfert/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'DWTransfert'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-detail.html',
                    controller: 'DWTransfertDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'DWTransfert', function($stateParams, DWTransfert) {
                    return DWTransfert.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'd-w-transfert',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('d-w-transfert-detail.edit', {
            parent: 'd-w-transfert-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-dialog.html',
                    controller: 'DWTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWTransfert', function(DWTransfert) {
                            return DWTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-transfert.new', {
            parent: 'd-w-transfert',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-dialog.html',
                    controller: 'DWTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null,
                                status: "CREATED",
                                scheduling: "EVERY_DAY_WINDOW",
                                rank: 0,
                                source : {
                                	account : {
                                		id: null,
                                	},
                                	path: null
                                },
                                target : {
                                	account : {
                                		id: null,
                                	},
                                	path: null
                                },
                                start: null,
                                end: null,
                                dayBegin: 0,
                                dayEnd: 21000,
                                nbError: 0,
                                errorMessages: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('d-w-transfert', null, { reload: true });
                }, function() {
                    $state.go('d-w-transfert');
                });
            }]
        })
        .state('d-w-transfert.edit', {
            parent: 'd-w-transfert',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-dialog.html',
                    controller: 'DWTransfertDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['DWTransfert', function(DWTransfert) {
                            return DWTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-transfert', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-transfert.delete', {
            parent: 'd-w-transfert',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-delete-dialog.html',
                    controller: 'DWTransfertDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['DWTransfert', function(DWTransfert) {
                            return DWTransfert.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('d-w-transfert', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('d-w-transfert.deleteDone', {
            parent: 'd-w-transfert',
            url: '/deleteDone',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/d-w-transfert/d-w-transfert-deletedone-dialog.html',
                    controller: 'DWTransfertDeleteDoneController',
                    controllerAs: 'vm',
                    size: 'md'
                }).result.then(function() {
                    $state.go('d-w-transfert', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
