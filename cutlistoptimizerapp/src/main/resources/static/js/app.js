
var app = angular.module("app", ['ui.grid', 'ui.grid.edit', 'ui.grid.rowEdit', 'ui.grid.cellNav', 'ui.grid.selection', 'ui.sortable', 'ui.bootstrap', 'pascalprecht.translate']);

app.config(['$translateProvider', '$windowProvider', '$locationProvider', function($translateProvider, $windowProvider, $locationProvider) {

    var $window = $windowProvider.$get();
    var language = $window.localStorage.getItem("language");

    if (language === null) {
        language = window.navigator.userLanguage || window.navigator.language;
        language = language.substring(0, 2);
    }

    $translateProvider.translations('en', translationsEN);
    $translateProvider.translations('pt', translationsPT);
    $translateProvider.preferredLanguage(language);
    $translateProvider.fallbackLanguage('en');

    //$locationProvider.html5Mode(true);
}]);

app.service('TilingService', function($http, $location, TilingData, DrawService) {

    this.serverBaseUrl = $location.protocol() + '://'+ $location.host() +':'+  $location.port();
    if ($location.host().indexOf("localhost") ==! -1) {
        this.serverBaseUrl = 'http://localhost:8080';
    }

    var taskId;

    this.requestTiling = function(tiles, stockTiles, cfg, callback) {

        var data = {tiles: tiles, baseTiles: stockTiles, configuration: cfg};

        // Generate an unique task id
        taskId = "" + new Date().getTime() + JSON.stringify(data).hashCode();
        cfg.taskId = taskId;



        if (typeof android !== 'undefined') {
            var response = android.compute(angular.toJson(data));
            callback(response);
        } else {
            $http.post(this.serverBaseUrl + '/compute-tilling', data)
                .then(function (response) {
                    callback(response.data);
                }, function(error) {
                    // TODO
                });
        }
    }

    this.cancelTiling = function(callback) {
        if (typeof android !== 'undefined') {
            android.stopTask(taskId);
        } else {
            $http.post(this.serverBaseUrl + '/stop-task/' + taskId)
                .then(function(response) {
                }, function(error) {
                    // TODO
                });
        }
    };

    this.getTaskStatus = function(callback) {

        if (typeof android !== 'undefined') {
            var data = android.getTaskStatus(taskId);
            data = angular.fromJson(data);

            if (data && data.solution) {
                TilingData.setData(data.solution);

            }

            return data;
        } else {
            $http.get(this.serverBaseUrl + '/task-status/' + taskId)
                .then(function (response) {
                    callback(response.data);
                    if (response.data.solution) {
                        TilingData.setData(response.data.solution);
                    }
                }, function(error) {
                    // TODO
                });
        }
    }
});

app.controller('Tiling', function(TilingService, TilingData, DrawService, $window, $scope, $location, $http, $timeout, $q, $translate, $interval, $anchorScroll, uiGridConstants) {


    $timeout(function() {
        //document.getElementById('splash').style.display = 'none';
        $("#splash").fadeOut();
    }, 0);

    var localStorageKeySuffix = '?v=1.2';

    var count = $window.localStorage.getItem("runCount");
    count = count ? parseInt(count) + 1 : 1;
    $window.localStorage.setItem("runCount", count);
    if (typeof android !== 'undefined') {
        android.audit("Run count: " + count);
    }


    $scope.drawService = DrawService;

    $scope.tiling = TilingData;

    $scope.Math = Math;

    $scope.invalidData = true;

    $scope.isLoading = false;

    $scope.visibleTileInfoIdx = 0;

    $scope.requestStatus;
    $scope.statusMessage;

    // Workaround for changing grid label translations without reloading the page
    $scope.isGridReloding = false;

    $scope.changeLanguage = function (langKey) {

        if (typeof android !== 'undefined') {
            android.audit("Change language to " + langKey);
        }

        $translate.use(langKey);

        $scope.isGridReloding = true;
        setupTilesGrid();
        setupStockTilesGrid();

        $timeout(function() {
            $scope.isGridReloding = false;
        }, 0);

        $window.localStorage.setItem("language", langKey);
    };

    $scope.incrementVisibleTileInfoIdx = function() {
        if ($scope.visibleTileInfoIdx < $scope.tiling.getNbrMosaics() - 1) {
            $scope.visibleTileInfoIdx++;
        }
    };

    $scope.decrementVisibleTileInfoIdx = function() {
        if ($scope.visibleTileInfoIdx > 0) {
            $scope.visibleTileInfoIdx--;
        }
    };

    $scope.scrollTo = function(id) {
        $("body").animate({scrollTop: $("#" + id).offset().top - 45}, "slow");
    };

    /**
     * Handle window resizing
     */
    angular.element($window).bind('resize', function() {
        render();
    });

    $scope.isTilesGridVisible = false;
    $scope.isStockGridVisible = false;

    $scope.isBaseOptionsCollapsed = false;
    $scope.isAdvancedOptionsCollapsed = $window.innerWidth < 768? true : true;

    $scope.isTilingInfoVisible = true;

    $scope.isCutListCollapsed = false;


    $scope.toggleIsTilingInfoVisible = function() {
        $scope.isTilingInfoVisible = !$scope.isTilingInfoVisible;
        $timeout(function() {
            render();
        });
    };


    // Try to load cfg from url params
    $scope.cfg = angular.fromJson($location.search().cfg);

    if (!$scope.cfg) {
        $scope.cfg = angular.fromJson($window.localStorage.getItem("cfg" + localStorageKeySuffix));
    }

    if ($scope.cfg === null) {
        $scope.cfg = {
            version: 1.0,
            cutThickness: 0,
            allowTileRotation: true,
            forceOneBaseTile: false,
            accuracyFactor: 0,  // TODO: Only for debug purposes
            priorities: [
                "LEAST_WASTED_AREA",
                "LEAST_NBR_CUTS",
                "LEAST_NBR_UNUSED_TILES",
                "BIGGEST_UNUSED_TILE_AREA",
                "SMALLEST_CENTER_OF_MASS_DIST_TO_ORIGIN",
                "MOST_UNUSED_PANEL_AREA",
                "MOST_HV_DISCREPANCY",
                "MOST_NBR_MOSAICS"]
        };
    } else {
        if ($scope.cfg.version !== "1.4") {
            $scope.cfg.version = "1.4";
            $scope.cfg.priorities = [
                "LEAST_WASTED_AREA",
                "LEAST_NBR_CUTS",
                "LEAST_NBR_UNUSED_TILES",
                "BIGGEST_UNUSED_TILE_AREA",
                "MOST_HV_DISCREPANCY",
                "HIGHER_PERMUTATION_PRIORITY",
                "SMALLEST_CENTER_OF_MASS_DIST_TO_ORIGIN",
                "MOST_NBR_MOSAICS"];
        }
    }

    /*****************************************************************************************************
     * Watchers
     *****************************************************************************************************/

    $scope.$watchCollection('cfg.priorities', function(newValue, oldValue) {
        if (newValue !== oldValue) {
            $scope.invalidData = true;
            saveDataLocalStorage();
        }
    });

    $scope.$watchGroup(['cfg.accuracyFactor', 'cfg.cutThickness', 'cfg.allowTileRotation', 'cfg.forceOneBaseTile', 'cfg.priorities'], function(newValue, oldValue) {
        if (newValue !== oldValue) {

            if ($scope.cfg.cutThickness < 0) {
                $scope.cfg.cutThickness = 0;
            }

            if ($scope.cfg.accuracyFactor < 0) {
                $scope.cfg.accuracyFactor = 0;
            }

            $scope.invalidData = true;
            saveDataLocalStorage();
        }
    });

    /**
     * Redraw canvas if data is changed
     */
    $scope.$watch('tiling',function(newValue, oldValue) {
        if (newValue !== oldValue) {
            render();
        }
    }, true);

    $scope.$watch('tiles', function(newValue, oldValue) {
        if (newValue !== oldValue) {
            validateTilesArray();
            $scope.invalidData = true;
            saveDataLocalStorage();
        }
    }, true);

    $scope.$watch('stockTiles', function(newValue, oldValue) {
        if (newValue !== oldValue) {
            validateStockTilesArray();
            $scope.invalidData = true;
            saveDataLocalStorage();
        }
    }, true);

    function addNewTile() {
        var tileId = -1;
        angular.forEach($scope.tiles, function(tile) {
            tileId = Math.max(tile.id, tileId);
        });
        $scope.tiles.push({width: null, height: null, count: null, enabled: true, id: tileId + 1});
    }

    function addNewStockTile() {
        $scope.stockTiles.push({width: null, height: null, count: null, enabled: true});
    }

    /**
     * Gets the number of used and valid tiles.
     * @returns {number}
     */
    $scope.getNbrUsedTiles = function(includeDisabled) {
        var count = 0;
        angular.forEach($scope.tiles, function(tile) {
            if (tile.width && tile.height && tile.count && (tile.enabled || includeDisabled)) {
                count++;
            }
        });
        return count;
    };

    /**
     * Gets the number of used and valid base tiles.
     * @returns {number}
     */
    $scope.getNbrUsedStockTiles = function(includeDisabled) {
        var count = 0;
        angular.forEach($scope.stockTiles, function(tile) {
            if (tile.width && tile.height && tile.count && (tile.enabled || includeDisabled)) {
                count++;
            }
        });
        return count;
    };

    // Try to load tiles from url params
    $scope.stockTiles = angular.fromJson($location.search().stockTiles);

    if (!$scope.stockTiles) {
        $scope.stockTiles = angular.fromJson($window.localStorage.getItem("baseTiles" + localStorageKeySuffix));
    }

    var resetStockTiles = function() {

        // {width: null, height: null, count: null, enabled: true}];
        $scope.stockTiles.length = 0;
        addNewStockTile();
        addNewStockTile();
        addNewStockTile();
        addNewStockTile();
        addNewStockTile();
    };
    if ($scope.stockTiles === null) {
        $scope.stockTiles = [
            {width: 600, height: 300, count: 10, enabled: true, isUsed: false},
            {width: 600, height: 400, count: 10, enabled: true, isUsed: false},
            {width: 800, height: 300, count: 10, enabled: true, isUsed: false},
            {width: 800, height: 400, count: 10, enabled: true, isUsed: false},
            {width: 1200, height: 300, count: 10, enabled: true, isUsed: false},
            {width: 1200, height: 400, count: 10, enabled: true, isUsed: false},
            {width: 1200, height: 600, count: 10, enabled: true, isUsed: false},
            {width: 1200, height: 800, count: 10, enabled: true, isUsed: false},
            {width: 2440, height: 400, count: 10, enabled: true, isUsed: false},
            {width: 2440, height: 600, count: 10, enabled: true, isUsed: false},
            {width: 2440, height: 800, count: 10, enabled: true, isUsed: false},
            {width: 2440, height: 1222, count: 10, enabled: true, isUsed: false},
            {width: 2500, height: 1250, count: 10, enabled: true, isUsed: false},
            {width: null, height: null, count: null, enabled: true, isUsed: false}]; // TODO: How to add a new one?
        // $scope.stockTiles = [];
        // resetStockTiles();
    }

    $scope.sort = function(tiles) {
        tiles.sort(function(a, b) {

            // Tiles with null dimensions will be last.
            if (a.width === null || a.height === null) return 1;
            if (b.width === null || a.height === null) return 0;

            // If tiles have have the same area, the one with greatest width will go first.
            if (a.width * a.height === b.width * b.height) return a.width - b.width;

            // Tiles with greater area will go first.
            return (a.width * a.height) - (b.width * b.height);
        });
    };

    $scope.sortStockTiles = function() {
        $scope.stockTiles.sort(function(a, b) { return (a.width * a.height) - (b.width * b.height); });
    };


    // Try to load tiles from url params
    $scope.tiles = angular.fromJson($location.search().tiles);

    if (!$scope.tiles) {
        $scope.tiles = angular.fromJson($window.localStorage.getItem("tiles" + localStorageKeySuffix));
    }

    var resetTiles = function() {
        $scope.tiles.length = 0;
        addNewTile();
        addNewTile();
        addNewTile();
        addNewTile();
        addNewTile();
    };
    if ($scope.tiles === null) {
        $scope.tiles = [];
        resetTiles();
    }

    $scope.gridOptions = {
        enableCellEditOnFocus: true,
        enableGridMenu: true,
        enableColumnMenus: false,
        rowTemplate:'template/tile-row-template.html',
        rowHeight: 26,
        enableHorizontalScrollbar: 0,
        gridMenuShowHideColumns: false,
        enableSorting: false,
        data: $scope.tiles
    };

    function setupTilesGrid() {

        $scope.gridOptions.gridMenuCustomItems = [
            {
                title: $translate.instant('SELECT_ALL'),
                action: function ($event) {
                    $scope.tiles.forEach(function(obj) {
                        obj.enabled = true;
                    });
                    $scope.invalidData = true;
                    saveDataLocalStorage();
                },
                order: 300
            },
            {
                title: $translate.instant('SELECT_NONE'),
                action: function ($event) {
                    $scope.tiles.forEach(function(obj) {
                        obj.enabled = false;
                    });
                    $scope.invalidData = true;
                    saveDataLocalStorage();
                },
                order: 301
            },
            {
                title: $translate.instant('DELETE_ALL'),
                action: function ($event) {
                    // TODO: Translate msg
                    if (confirm("Delete all panels?") === true) {
                        resetTiles();
                    }
                },
                order: 302
            },
            {
                title: $translate.instant('SORT'),
                action: function ($event) {
                    $scope.sort($scope.tiles);
                },
                order: 303
            }
        ];

        $scope.gridOptions.columnDefs = [
            { name: 'width', displayName: $translate.instant('WIDTH'), enableCellEdit: true, type: 'number',  width: '26%'},
            { name: 'height', displayName: $translate.instant('HEIGHT'), enableCellEdit: true, type: 'number',  width: '26%'},
            { name: 'count', displayName: $translate.instant('QUANTITY'), enableCellEdit: true, type: 'number',  width: '18%'},
            { name: ' ', displayName: ' ', width: '30%', allowCellFocus : false, enableCellEdit: false, cellTemplate: 'template/tile-options-cell-template.html'}
        ];
    }
    setupTilesGrid();

    $scope.stockGridOptions = {
        enableCellEditOnFocus: true,
        enableGridMenu: true,
        enableColumnMenus: false,
        exporterMenuPdf: true,
        rowHeight: 26,
        rowTemplate:'template/stock-tile-row-template.html',
        enableHorizontalScrollbar: 0,
        gridMenuShowHideColumns: false,
        enableSorting: false,
        data: $scope.stockTiles
    };

    function setupStockTilesGrid() {

        $scope.stockGridOptions.gridMenuCustomItems = [
            {
                title: $translate.instant('SELECT_ALL'),
                action: function ($event) {
                    $scope.stockTiles.forEach(function(obj) {
                        obj.enabled = true;
                    });
                    $scope.invalidData = true;
                    saveDataLocalStorage();
                },
                order: 1
            },
            {
                title: $translate.instant('SELECT_NONE'),
                action: function ($event) {
                    $scope.stockTiles.forEach(function(obj) {
                        obj.enabled = false;
                    });
                    $scope.invalidData = true;
                    saveDataLocalStorage();
                },
                order: 2
            },
            {
                title: $translate.instant('DELETE_ALL'),
                action: function ($event) {
                    // TODO: Translate msg
                    if (confirm("Delete all stock panels?") === true) {
                        resetStockTiles();
                    }
                },
                order: 3
            },
            {
                title: $translate.instant('SORT'),
                action: function ($event) {
                    $scope.sort($scope.stockTiles);
                },
                order: 303
            }
        ];

        $scope.stockGridOptions.columnDefs = [
            { name: 'width', displayName: $translate.instant('WIDTH'), enableCellEdit: true, enableColumnMenu: false, type: 'number',  width: '26%'},
            { name: 'height', displayName: $translate.instant('HEIGHT'), enableCellEdit: true, enableColumnMenu: false, type: 'number',  width: '26%'},
            { name: 'count', displayName: $translate.instant('QUANTITY'), enableCellEdit: true, type: 'number',  width: '18%' },
            { name: ' ', displayName: ' ', width: '30%', allowCellFocus : false, enableCellEdit: false, enableColumnMenu: false, enableSorting: false, cellTemplate: 'template/stock-tile-options-cell-template.html'}
        ];
    }
    setupStockTilesGrid();

    $scope.saveRow = function(rowEntity) {
        // Create a fake promise - normally you'd use the promise returned by $http or $resource
        var promise = $q.defer();
        $scope.gridApi.rowEdit.setSavePromise( rowEntity, promise.promise );
        promise.resolve();
    };

    $scope.gridOptions.onRegisterApi = function(gridApi) {
        // Set gridApi on scope
        $scope.gridApi = gridApi;
        gridApi.rowEdit.on.saveRow($scope, $scope.saveRow);
    };

    $scope.saveBaseTileRow = function(rowEntity ) {
        // Create a fake promise - normally you'd use the promise returned by $http or $resource
        var promise = $q.defer();
        $scope.gridApiBaseTiles.rowEdit.setSavePromise( rowEntity, promise.promise );
        promise.resolve();
    };

    $scope.stockGridOptions.onRegisterApi = function(gridApi) {
        // Set gridApi on scope
        $scope.gridApiBaseTiles = gridApi;
        gridApi.rowEdit.on.saveRow($scope, $scope.saveBaseTileRow);
    };

    function validateTilesArray() {
        if ($scope.getNbrUsedTiles(true) > $scope.tiles.length - 1) {
            addNewTile();
        }

        while ($scope.tiles.length < 5) {
            addNewTile();
        }

        $scope.tiles.forEach(function(tile) {

            // Do not allow decimal places
            try { tile.width = tile.width ? Math.floor(tile.width) : null; } catch (e) {};
            try { tile.height = tile.height ? Math.floor(tile.height) : null; } catch (e) {};

            if (tile.width && tile.height && !tile.count) {
                tile.count = 1;
                tile.isInvalid = false;
            }
        });
    }

    function validateStockTilesArray() {
        if ($scope.getNbrUsedStockTiles(true) > $scope.stockTiles.length - 1) {
            addNewStockTile();
        }

        while ($scope.stockTiles.length < 5) {
            addNewStockTile();
        }

        $scope.stockTiles.forEach(function(tile) {

            // Do not allow decimal places
            try { tile.width = tile.width ? Math.floor(tile.width) : null; } catch (e) {};
            try { tile.height = tile.height ? Math.floor(tile.height) : null; } catch (e) {};

            if (tile.width && tile.height && tile.count === null) {
                tile.count = 1;
                tile.isInvalid = false;
            }
        });
    }

    $scope.removeTile = function(tile) {
        var index = $scope.tiles.indexOf(tile);
        $scope.tiles.splice(index, 1);
        validateTilesArray();
        $scope.invalidData = true;
        saveDataLocalStorage();
    };

    $scope.removeStockTile = function(tile) {
        var index = $scope.stockTiles.indexOf(tile);
        $scope.stockTiles.splice(index, 1);
        validateStockTilesArray();
        $scope.invalidData = true;
        saveDataLocalStorage();
    };

    // $scope.zoomIn = function() {
    //     DrawService.setZoom(DrawService.getZoom() + 0.1);
    //     render();
    // };
    //
    // $scope.zoomOut = function() {
    //     DrawService.setZoom(DrawService.getZoom() - 0.1);
    //     render();
    // };

    $scope.print = function(divName) {
        window.print();
    };


    $scope.toggleTile = function(tile) {
        tile.enabled = !tile.enabled;
        $scope.invalidData = true;
        saveDataLocalStorage();
    };

    $scope.toggleBaseTile = function(tile) {
        tile.enabled = !tile.enabled;
        $scope.invalidData = true;
        saveDataLocalStorage();
    };

    $scope.compute = function() {
        DrawService.clear();
        TilingData.setData(null);
        requestTilling();

    };

    $scope.cancelTilingRequest = function() {
        $scope.isLoading = false;
        $scope.invalidData = true;
        TilingService.cancelTiling();
    };

    function requestTilling() {

        $scope.errorMessage = '';

        $scope.tiles.forEach(function(tile) {
            if (tile.width && tile.height && tile.count) {
                tile.isInvalid = false;
            }
        });

        $scope.stockTiles.forEach(function(tile) {
            if (tile.width && tile.height && tile.count) {
                tile.isInvalid = false;
            }
        });


        if ($scope.getNbrUsedTiles(false) === 0) {
            $scope.isGridReloding = true;
            $scope.tiles[0].isInvalid = true;

            $timeout(function () {
                $scope.isGridReloding = false;
            }, 0);

            if (typeof android !== 'undefined') {
                android.showToast($translate.instant('MSG_NO_PANELS'));
            }

            $scope.errorMessage += "\n" + $translate.instant('MSG_NO_PANELS');
        }

        if ($scope.getNbrUsedStockTiles(false) === 0) {
            $scope.isGridReloding = true;
            $scope.stockTiles[0].isInvalid = true;

            $timeout(function () {
                $scope.isGridReloding = false;
            }, 0);

            if (typeof android !== 'undefined') {
                android.showToast($translate.instant('MSG_NO_STOCK_PANELS'));
            }

            $scope.errorMessage += "\n" + $translate.instant('MSG_NO_STOCK_PANELS');
        }

        if ($scope.getNbrUsedTiles(true) === 0 || $scope.getNbrUsedStockTiles(true) === 0) {
            return;
        }



        var biggestTileWidth = 0;
        var biggestTileHeight = 0;
        var biggestTileArea = Number.MAX_SAFE_INTEGER;
        var totalTiles = 0;

        angular.forEach($scope.tiles, function(tile) {
            if (tile.width && tile.width * tile.height < biggestTileArea) {
                biggestTileArea = tile.width * tile.height;
                biggestTileWidth = tile.width;
                biggestTileHeight = tile.height;
                totalTiles += tile.count;
            }
        });


        var biggestStockTileWidth = 0;
        var biggestStockTileHeight = 0;
        var biggestStockTileArea = 0;
        var totalBaseTiles = 0;

        angular.forEach($scope.stockTiles, function(stockTile) {
            if (stockTile.width && stockTile.width * stockTile.height > biggestStockTileArea) {
                biggestStockTileArea = stockTile.width * stockTile.height;
                biggestStockTileWidth = stockTile.width;
                biggestStockTileHeight = stockTile.height;
                totalBaseTiles += stockTile.count;
            }
        });

        if (biggestTileArea > biggestStockTileArea) {
            if (confirm("Unable to cut specified panels from available stock.\nRequired panels are bigger than stock! Are the specification dimensions misplaced? Should tiles be swapped with the stock?") === true) {
                var tilesBak = $scope.tiles.slice();

                $scope.tiles.length = 0;
                [].push.apply($scope.tiles, $scope.stockTiles);

                $scope.stockTiles.length = 0;
                [].push.apply($scope.stockTiles, tilesBak);
            } else {
                $scope.errorMessage = $translate.instant('NO_SOLUTION');
                return;
            }
        }








        $scope.isLoading = true;


        TilingService.requestTiling($scope.tiles, $scope.stockTiles, $scope.cfg, function(response) {
            $scope.requestStatus = response;
        });

        var scrolled = false;   // Whether already received a solution and scrolled to it. Used to scroll only once.
        var poller = function() {

            var callback = function(data) {

                if (data) {
                    $scope.statusMessage = data.statusMessage;
                }

                if ($scope.statusMessage === 'Finished' || $scope.requestStatus != 0) {
                    $scope.statusMessage = null;
                    $scope.isLoading = false;
                    $timeout(function() { $scope.invalidData = false; }, 100);


                    $scope.scrollTo('main-content');
                }

                if ($scope.statusMessage && $window.innerWidth < 768 && $scope.tiling.getNbrMosaics() > 0) {
                    $scope.statusMessage = $scope.statusMessage + "\nTap to accept solution";
                }

                if ($scope.isLoading) {
                    // Reschedule polling
                    $timeout(poller, 1000);

                    if (TilingData.getData() && !scrolled) {
                        // Scroll to the presented solution
                        $scope.scrollTo('main-content');
                        // Scrolled once, don't want to scroll again.
                        scrolled = true;
                    }
                }
            };

            var data = TilingService.getTaskStatus(callback);
            if (typeof android !== 'undefined') {
                callback(data);
            }
        };
        $timeout(poller, 1000);
    }

    function saveDataLocalStorage() {
        $window.localStorage.setItem("tiles" + localStorageKeySuffix, angular.toJson($scope.tiles));
        $window.localStorage.setItem("baseTiles" + localStorageKeySuffix, angular.toJson($scope.stockTiles));
        $window.localStorage.setItem("cfg" + localStorageKeySuffix, angular.toJson($scope.cfg));
    }

    function render() {

        // Clean used status from stock tiles
        angular.forEach($scope.stockTiles, function(stockTile) {
            stockTile.isUsed = false;
        });


        if (TilingData.getData()) {
            // Loop through all mosaics for this solution
            angular.forEach(TilingData.getData().mosaics, function (mosaic, index) {

                // Render non final tiles
                angular.forEach(mosaic.tiles, function (tile, index) {

                    angular.forEach($scope.stockTiles, function (stockTile) {
                        //stockTile.isUsed = false;
                        if (stockTile.width == tile.width && stockTile.height == tile.height) {
                            stockTile.isUsed = true;
                        }
                    });
                });
            });
        }

        saveDataLocalStorage();
        $scope.visibleTileInfoIdx = 0;

        $timeout(function() {
            DrawService.init();
            DrawService.renderTiles();
        });
    }

    /**
     * Clears local storage and reloads the page
     */
    $scope.reset = function() {
        if (confirm("Clear all data?") === true) {
            if (typeof android !== 'undefined') {
                android.audit("Clear all data");
            }
            var count = $window.localStorage.getItem("runCount");
            localStorage.clear();
            $window.localStorage.setItem("runCount", count);
            $window.location.reload(true);
            $location.search('tiles', null);
            $location.search('stockTiles', null);
        }
    };


    if ($location.search().compute) {
        $scope.compute();
    }

    $scope.dataToUrl = function() {
        $location.search('tiles', angular.toJson($scope.tiles));
        $location.search('stockTiles', angular.toJson($scope.stockTiles));
        $location.search('cfg', angular.toJson($scope.cfg));
        $location.search('compute', true);
    };

    $scope.showSvg = function() {
        $scope.statusMessage = null;
        $scope.isLoading = false;

        if (TilingData.getData().mosaics && TilingData.getData().mosaics.length > 0) {
            $scope.scrollTo('main-content');
        }
    };

    $scope.generatePdf = function() {

        var margin = 25;

        if (!$scope.tiling.getNbrMosaics()) {
            if (typeof android !== 'undefined') {
                android.showToast("No data");
            } else {
                // TODO
            }
            return;
        }

        if (typeof android !== 'undefined') {
            android.showToast("Creating PDF file...");
        }

        DrawService.reset(true);

        window.scrollTo(0, 0);
        document.getElementById('pdf-info').style.display = 'block';
        var elemWidth = document.getElementById('pdf-info').offsetWidth;
        var elemHeight = document.getElementById('pdf-info').offsetHeight;

        // Get svg markup as string
        var svg = document.getElementById('svg-canvas').innerHTML;

        if (svg) {
            svg = svg.replace(/\r?\n|\r/g, '').trim();
        }

        var canvas = document.createElement('canvas');
        var context = canvas.getContext('2d');
        canvg(canvas, svg);


        var imgData = canvas.toDataURL('image/jpeg');

        // Generate PDF
        var doc = new jsPDF('p', 'pt', 'a4');
        var width = doc.internal.pageSize.width;
        var height = doc.internal.pageSize.height;

        if (elemHeight < height) {
            // Stretch image to fit page with, height will be proportional.
            doc.addImage(imgData, 'JPEG', margin, margin, width-225, 0);
        } else {
            // Stretch image to fit page height, with will be proportional.
            doc.addImage(imgData, 'JPEG', margin, margin, 0, height - margin);
        }

        var canvas123 = html2canvas(document.getElementById('pdf-info'), {background:'#fff',
            onrendered: function(canvas) {
                var imgData2 = canvas.toDataURL('image/jpeg');
                doc.addImage(imgData2, 'JPEG', width-200, 25, 200, elemHeight * 200 / elemWidth);

                if (typeof android !== 'undefined') {
                    var pdfData = doc.output('dataurlstring');
                    android.savePdf(pdfData);
                } else {
                    var today = new Date();
                    var filename = 'cutlist-optimizer_';
                    filename += today.getFullYear().toString();
                    filename += '-' + ("0" + (today.getMonth()+1).toString()).slice(-2);
                    filename += '-' + ("0" + today.getDate().toString()).slice(-2);
                    filename += '_' + ("0" + today.getHours().toString()).slice(-2);
                    filename += '-' + ("0" + today.getMinutes().toString()).slice(-2);
                    filename += '-' + ("0" + today.getSeconds().toString()).slice(-2);
                    filename += '.pdf';
                    doc.save(filename);
                }

                DrawService.reset(false);
            }
        });

        document.getElementById( 'pdf-info' ).style.display = 'none';
    };
});

String.prototype.hashCode = function() {
    var hash = 0, i, chr, len;
    if (this.length === 0) return hash;
    for (i = 0, len = this.length; i < len; i++) {
        chr   = this.charCodeAt(i);
        hash  = ((hash << 5) - hash) + chr;
        hash |= 0; // Convert to 32bit integer
    }
    return hash;
};
