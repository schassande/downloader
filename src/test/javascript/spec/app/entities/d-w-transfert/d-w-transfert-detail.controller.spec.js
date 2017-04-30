'use strict';

describe('Controller Tests', function() {

    describe('DWTransfert Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockDWTransfert, MockDwFileInfo;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockDWTransfert = jasmine.createSpy('MockDWTransfert');
            MockDwFileInfo = jasmine.createSpy('MockDwFileInfo');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'DWTransfert': MockDWTransfert,
                'DwFileInfo': MockDwFileInfo
            };
            createController = function() {
                $injector.get('$controller')("DWTransfertDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'downloaderApp:dWTransfertUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
