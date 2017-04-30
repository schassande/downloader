'use strict';

describe('Controller Tests', function() {

    describe('DWFileInfo Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockDWFileInfo, MockDwHostAccount;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockDWFileInfo = jasmine.createSpy('MockDWFileInfo');
            MockDwHostAccount = jasmine.createSpy('MockDwHostAccount');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'DWFileInfo': MockDWFileInfo,
                'DwHostAccount': MockDwHostAccount
            };
            createController = function() {
                $injector.get('$controller')("DWFileInfoDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'downloaderApp:dWFileInfoUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
