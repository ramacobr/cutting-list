app.service('TilingData', function() {

    var data = {};

    var getData = function() {
        return this.data;
    };

    var getReturnCode = function() {
        if (this.data) {
            return this.data.returnCode;
        }
        return null;
    };

    var getMosaics = function() {
        if (this.data) {
            return this.data.mosaics;
        }
        return [];
    };

    var getNoFitTiles = function() {
        if (this.data) {
            return this.data.noFitTiles;
        }
        return [];
    };

    var getNbrNoFitTiles = function() {
        if (this.data && this.data.noFitTiles) {
            return this.data.noFitTiles.length;
        }
        return 0;
    };

    var getCuts = function(mosaicIndex) {
        return this.data.mosaics[mosaicIndex].cuts;
    };

    var getNbrCuts = function() {
        var count = 0;
        if (this.data && this.data.mosaics) {
            this.data.mosaics.forEach(function(mosaic) {
                if (mosaic.cuts) {
                    count += mosaic.cuts.length;
                }
            });
        }
        return count;
    };

    var getNbrMosaics = function() {
        if (this.data) {
            return this.data.mosaics.length;
        }
        return 0;
    };

    var setData = function(data) {
        this.data = data;
    };

    var setSvgContainer = function(mosaicIndex, svgContainer) {
        this.data.mosaics[mosaicIndex].svgContainer = svgContainer;
    };

    return {
        getReturnCode: getReturnCode,
        getData: getData,
        setData: setData,
        setData: setData,
        getMosaics: getMosaics,
        getNoFitTiles: getNoFitTiles,
        getNbrNoFitTiles: getNbrNoFitTiles,
        getCuts: getCuts,
        getNbrCuts: getNbrCuts,
        getNbrMosaics: getNbrMosaics,
        setSvgContainer: setSvgContainer
    };
});
