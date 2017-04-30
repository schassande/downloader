(function() {
    'use strict';

    angular
        .module('downloaderApp')
        .controller('DWTransfertDialogController', DWTransfertDialogController);

    DWTransfertDialogController.$inject = ['$q', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DWTransfert', 'DWHostAccount', 'FileBrowser'];

    function DWTransfertDialogController ($q, $timeout, $scope, $stateParams, $uibModalInstance, entity, DWTransfert, DWHostAccount, FileBrowser) {
        var vm = this;

        vm.source = {
        		files : [],
        		sortOrder   : -1,
        		sortType    : 'filename',
        		sortReverse : false,
        		searchFish  : '',
        		connected   : 0
        	};
        vm.target = {
        		files : [],
        		sortOrder   : -1,
        		sortType    : 'filename',
        		sortReverse : false,
        		searchFish  : '',
        		connected   : 0
        	};

        vm.dWTransfert = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.dwHostAccounts = DWHostAccount.query();

        vm.datePickerOpenStatus.start = false;
        vm.datePickerOpenStatus.end = false;
	    vm.selectSourceAccount = selectSourceAccount;
	    vm.selectTargetAccount = selectTargetAccount;
        vm.onBrowseSource = onBrowseSource;
        vm.onBrowseTarget = onBrowseTarget;
        vm.toOctets = toOctets;
        vm.selectSourceDirectory = selectSourceDirectory;
        vm.selectTargetDirectory = selectTargetDirectory;
        vm.selectSourceFile = selectSourceFile;
        vm.manageSourceConnection = manageSourceConnection;
        vm.manageTargetConnection = manageTargetConnection;

        
        if (!entity.id) {
            loadAll();
            function loadAll() {
                DWTransfert.maxRank(function(maxRank) {
                	vm.dWTransfert.rank = maxRank.maxRank + 1; 
                	vm.maxRank = maxRank.maxRank;
                });
            }
        }
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            var selectedSrcFiles = vm.source.files.filter(function(f){ return f.selected;});
            if (vm.dWTransfert.id == null) {
            	//create new transfert(s)
                if (selectedSrcFiles.length == 0) {
                	//no selected file
                	//=> show a message indicating the user must select a file or directory
                	console.log("No selected source file.")
                	onSaveError();
                } else {
                	var transferts = [];
	                for(var idx in selectedSrcFiles) {
		                var transfert = JSON.parse(JSON.stringify(vm.dWTransfert));
		                transfert.source.path = addPathElement(transfert.source.path, selectedSrcFiles[idx].filename, transfert.source.account.pathSeparator);
		                transferts.push(transfert);
	                }
	                var multipleTransfert = {
	                		transferts : transferts,
	                };
	                DWTransfert.createMultiple(multipleTransfert, onSaveSuccess, onSaveError);
                }
            } else {
            	DWTransfert.update(vm.dWTransfert, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('downloaderApp:dWTransfertUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
        
        
        function selectSourceAccount(){
        	if (vm.dWTransfert.source) {
        		vm.dWTransfert.source.path = vm.dWTransfert.source.account.defaultPath;
        		onBrowseSource();
        	}
        }
	    function selectTargetAccount(){
	    	if (vm.dWTransfert.target) {
	    		vm.dWTransfert.target.path = vm.dWTransfert.target.account.defaultPath;
	    		onBrowseTarget();
	    	}
	    }
	    
        function onBrowseSource() {
        	if (vm.dWTransfert.id) {
        		return;
        	}
        	var req = {
        		path : vm.dWTransfert.source.path,
        		account : vm.dWTransfert.source.account,
        		directoryOnly : false
        	}
        	FileBrowser.browse(req, onBrowseSourceResponse, onBrowseFail);
        }
        function onBrowseTarget() {
        	var req = {
        		path : vm.dWTransfert.target.path,
        		account : vm.dWTransfert.target.account,
        		directoryOnly : true
        	}
        	FileBrowser.browse(req, onBrowseTargetResponse, onBrowseFail);
        }
        
        function onBrowseSourceResponse(browseResponse) {
        	vm.dWTransfert.source.path = browseResponse.path;
        	if (browseResponse.files) {
        		vm.source.files = filterFiles(browseResponse.files);
        	} else {
        		console.log("browseResponse: " + JSON.stringify(browseResponse));
        	}
        }
        function onBrowseTargetResponse(browseResponse) {
        	vm.dWTransfert.target.path = browseResponse.path;
        	if (browseResponse.files) {
        		vm.target.files = filterFiles(browseResponse.files);
        	} else {
        		console.log("browseResponse: " + JSON.stringify(browseResponse));
        	}
        }
        function filterFiles(files) {
        	return files.filter(function(file) {
        		file.selected = false;
        		return file.filename == '..' || !file.filename.startsWith('.');
        	});
        }
        function onBrowseFail() {
        	console.log('onBrowseFail!')
        }

        function toOctets(size) {
        	if (size > (1024*1024*1024)) {
        		return (size / (1024*1024*1024)).toFixed(2) + ' Go';
        	} else if (size > (1024*1024)) {
        		return (size / (1024*1024)).toFixed(2) + ' Mo';
        	} else if (size > 1024) {
        		return (size / 1024).toFixed(2) + ' ko';
        	} else {
        		return size + ' o'
        	}
        }
        
        function selectSourceDirectory(file){
        	if (vm.dWTransfert.id) {
        		return;
        	}
        	if (file.dir) {
        		vm.dWTransfert.source.path = addPathElement(vm.dWTransfert.source.path, file.filename, vm.dWTransfert.source.account.pathSeparator);
        		onBrowseSource();
        	}
        }
        function selectTargetDirectory(file){
        	if (file.dir) {
        		vm.dWTransfert.target.path = addPathElement(vm.dWTransfert.target.path, file.filename, vm.dWTransfert.target.account.pathSeparator);
        		onBrowseTarget();
        	}
        }
        function addPathElement(path, element, pathSeparator) {
        	if (path.endsWith(pathSeparator)) {
            	return path + element;
        	} else {
            	return path + pathSeparator + element;
        	}
        }

        function selectSourceFile(file){
        	if (vm.dWTransfert.id) {
        		return;
        	}
        	console.log('File ' + file.filename + ' is ' + (file.selected ? 'selected.' : ' unselected'));
        	var srcFile = vm.source.files.find(function trouveCerises(f) { return f.filename == file.filename;});
        	if (srcFile) {
        		srcFile.selected = file.selected;
        	} else {
        		console.log("File '" + file.filename  +"' has not been found from source files.");
        	}

        }

        function manageSourceConnection(){
        	if (vm.dWTransfert.id) {
        		return;
        	}
        	if (vm.source.connected) {
        		vm.source.connected = 0;
            	FileBrowser.disconnect(vm.dWTransfert.source.account);
        	} else {
        		vm.source.connected = 1;
            	FileBrowser.connect(vm.dWTransfert.source.account);
        	}
        }
        function manageTargetConnection(){
        	if (vm.target.connected) {
        		vm.target.connected = 0;
            	FileBrowser.disconnect(vm.dWTransfert.target.account);
        	} else {
        		vm.target.connected = 1;
            	FileBrowser.connect(vm.dWTransfert.target.account);
        	}
        }

        function toDate(fileTime) {
        	return $filter('date')(new Date(fileTime), 'yyyy-MM-dd HH:mm')
        }
        vm.toDate = toDate;

    }
})();
