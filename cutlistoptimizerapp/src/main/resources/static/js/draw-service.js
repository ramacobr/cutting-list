app.factory('DrawService', function(TilingData, $window) {

    var isSvgPannable = true;

    var toggleIsSvgPannable = function() {
        isSvgPannable = !isSvgPannable;
        init();
        renderTiles();
    };

    function getIsSvgPannable() {
        return isSvgPannable;
    }

    var isDimensionsVisible = true;

    function toggleIsDimensionsVisible() {
        isDimensionsVisible = !isDimensionsVisible;
        if (isDimensionsVisible) {
            showDimensions();
        } else {
            removeDimensions();
        }
    }

    function getIsDimensionsVisible() {
        return isDimensionsVisible;
    }


    var zoom = 1;

    var dimensionsOffset = 15;

    var cutAnimDuration = 300;

    var color1 = '#d9534f';

    var tmpSvgOverlayElems = [];

    var data = {ratio: 1, zoom: 1};

    var svgContainer;

    var innerFontSize = 10;


    isHorizontal = function(obj) {
        return (obj.x2 - obj.x1) > (obj.y2 - obj.y1);
    };

    isVertical = function (obj) {
        return (obj.y2 - obj.y1) > (obj.x2 - obj.x1);
    };

    hasWidth = function(obj) {
        return obj.x2 != obj.x1;
    };

    hasHeight = function(obj) {
        return obj.y2 != obj.y1;
    };

    var gradients = [];

    function generateGradient(id, color, opacity1, opacity2) {

        gradients.push(id);

        var defs = svgContainer.append("defs")
            .append("linearGradient")
            .attr("id", id)
            .attr("x1", "0%")
            .attr("y1", "0%")
            .attr("x2", "100%")
            .attr("y2", "100%")
            .attr("spreadMethod", "pad");

        defs.append("stop")
            .attr("offset", "0%")
            .attr("stop-color", color)
            .attr("stop-opacity", opacity1);

        defs.append("stop")
            .attr("offset", "100%")
            .attr("stop-color", color)
            .attr("stop-opacity", opacity2);
    }

    function generateGradients() {
         // generateGradient("gradient", "#bd1e24", 0.1, 0.3);
         // generateGradient("gradient2", "#e97600", 0.1, 0.3);
         // generateGradient("gradient3", "#f6c700", 0.1, 0.3);
         // generateGradient("gradient4", "#007256", 0.1, 0.3);
         // generateGradient("gradient5", "#0067a7", 0.1, 0.3);
         // generateGradient("gradient6", "#964f8e", 0.1, 0.3);

        generateGradient("gradient", "#004B7A", 0.1, 0.25);
        generateGradient("gradient1", "#8A161B", 0.1, 0.25);
        generateGradient("gradient2", "#B39100", 0.1, 0.25);
        generateGradient("gradient3", "#00533F", 0.1, 0.25);
        generateGradient("gradient4", "#AA5600", 0.1, 0.25);
        generateGradient("gradient5", "#6E3A68", 0.1, 0.25);
    }



    function init() {

        // Reset zoom
        zoom = 1;

        // Get div element
        var div = document.getElementById("svg-canvas");
        if (!div) {
            return;
        }

        // Calculate the ratio based on the maximum mosaic width
        var maxWidth = 0;
        var tilesHeight = 0;
        angular.forEach(TilingData.getMosaics(), function(mosaic, index) {
            maxWidth = Math.max(mosaic.base.width, maxWidth);
            tilesHeight += mosaic.base.height;
        });
        var divWidth = div.clientWidth;

        // Find a ratio to fit horizontally
        var ratioH = data.zoom * (divWidth / (maxWidth * 1.13));

        // Find a ratio to fit verticaly
        var ratioV = data.zoom * ($window.innerHeight / (tilesHeight * 1.35));

        // Consider the smallest ratio to fit the mosaics
        //data.ratio = Math.min(ratioH, ratioV);
        data.ratio = ratioH;

        // Cap ration at 0.5
        //data.ratio = Math.max(data.ratio, 0.5);

        // Find offset
        var canvasHeight = 0;
        angular.forEach(TilingData.getMosaics(), function(mosaic, index) {

            canvasHeight += mosaic.base.height * data.ratio + 60;

            if (index > 0) {
                mosaic.yOffset = TilingData.getMosaics()[index - 1].yOffset + TilingData.getMosaics()[index - 1].base.height * data.ratio + 50;
            } else {
                mosaic.yOffset = 0;
            }
        });

        if ($window.innerWidth >= 990 && $window.innerHeight - 40 > canvasHeight) {
            canvasHeight = $window.innerHeight - 40;
        }




        d3.select("#svg-canvas").select("svg").remove();

        var svgContainer2 = d3.select("#svg-canvas").append("svg")
            .attr("width", divWidth)
            .attr("height", canvasHeight)
            .append("g");

        if (/*$window.innerWidth >= 768 &&*/ isSvgPannable) {    // To avoid dragging in mobile
            svgContainer2.call(d3.zoom().on("zoom", function () {
                svgContainer.attr("transform", d3.event.transform);
                zoom = d3.event.transform.k;
                cleanTmpSvgOverlayElems();
                drawBaseDimensions();
                removeDimensions();
                showDimensions();
                //console.log(d3.event.transform.k);
            }));
        }

        // Apply a left margin
        var xTranslation;
        if ($window.innerWidth >= 768) {
            xTranslation = 25;
        } else {
            xTranslation = 15;
            data.ratio *= 0.97;
        }

        // Apply margins
        svgContainer = svgContainer2.attr("transform", "translate(" + xTranslation + ", 25)").append("g");

        // White background
        svgContainer.append("rect").attr("x", -xTranslation).attr("y", -25).attr("width", "100%").attr("height", "100%").attr("fill", "white");

        generateGradients();
    }

    function clear() {
        d3.select("#svg-canvas").select("svg").remove();
    }


    function renderTiles() {

        if (!TilingData.getData() || !TilingData.getData().mosaics || TilingData.getData().mosaics.length === 0) {
            return;
        }

        // Loop through all mosaics for this solution
        angular.forEach(TilingData.getData().mosaics, function(mosaic, index) {

            drawBaseTile(mosaic);
            drawBaseDimensions(mosaic);
            drawTiles(mosaic);
            //drawCutAnimation(mosaic);
        });

        if (isDimensionsVisible) {
            showDimensions();
        }
    }


    function drawBaseTile(mosaic) {

        // filters go in defs element
        var defs = svgContainer.append("defs");

        // create filter with id #drop-shadow
        // height=130% so that the shadow is not clipped
        var filter = defs.append("filter")
            .attr("id", "drop-shadow")
            .attr("height", "130%");

        // SourceAlpha refers to opacity of graphic that this filter will be applied to
        // convolve that with a Gaussian with standard deviation 3 and store result
        // in blur
        filter.append("feGaussianBlur")
            .attr("in", "SourceAlpha")
            .attr("stdDeviation", 2)
            .attr("result", "blur");

        // translate output of Gaussian blur to the right and downwards with 2px
        // store result in offsetBlur
        filter.append("feOffset")
            .attr("in", "blur")
            .attr("dx", 1)
            .attr("dy", 1)
            .attr("result", "offsetBlur");

        // overlay original SourceGraphic over translated blurred opacity by using
        // feMerge filter. Order of specifying inputs is important!
        var feMerge = filter.append("feMerge");

        feMerge.append("feMergeNode")
            .attr("in", "offsetBlur")
        feMerge.append("feMergeNode")
            .attr("in", "SourceGraphic");

        // Draw the base tile
        var baseTile = svgContainer.append("rect")
            .attr("x", 0)
            .attr("y", mosaic.yOffset)
            .attr("width", mosaic.base.width * data.ratio)
            .attr("height", mosaic.base.height * data.ratio)
            .attr("vector-effect", "non-scaling-stroke")
            .style("stroke", "black")
            .style("stroke-width", "2")
            .classed('background', true)
            .attr('fill', "white")
            .style("filter", "url(#drop-shadow)");
    }



    function drawTiles(mosaic) {

        // Render non final tiles
        angular.forEach(mosaic.tiles, function(tile, index) {

            if (tile.hasChildren || tile.final === true) {
                // This tile has inner tiles, doesn't need to be rendered.
                return;
            }

            var rec = svgContainer.append("rect")
                .attr("x", tile.x * data.ratio)
                .attr("y", getTileY2(mosaic.base.height, tile.y + tile.height) + mosaic.yOffset)
                .attr("width", tile.width * data.ratio)
                .attr("height", tile.height * data.ratio)
                .style("stroke", "#bbb")
                .style("stroke-width", "1")
                .attr("vector-effect", "non-scaling-stroke")
                .attr('fill', "#f5f5f5")
                .classed('background', true)
                .on("mouseover", function () {
                    cleanTmpSvgOverlayElems();
                    drawDimensionH(tile.x, tile.x + tile.width, mosaic);
                    drawDimensionV(tile.y, tile.y + tile.height, mosaic);
                    if (isSvgPannable) {
                        d3.select(this).style("cursor", "move")
                    }
                })
                .on("mouseout", function () {
                    cleanTmpSvgOverlayElems();
                    drawBaseDimensions(mosaic);
                    if (isSvgPannable) {
                        d3.select(this).style("cursor", "default")
                    }
                })
                .on("click", function (x1, y1, x2, y2) {
                    return function () {
                        //alert('test');
                    };
                }(tile.x1, tile.y1, tile.x2, tile.y2));
        });

            // Render final tiles
            angular.forEach(mosaic.tiles, function (tile, index) {

                if (tile.hasChildren || tile.final === false) {
                    // This tile has inner tiles, doesn't need to be rendered.
                    return;
                }

                var rec = svgContainer.append("rect")
                    .attr("x", tile.x * data.ratio)
                    .attr("y", getTileY2(mosaic.base.height, tile.y + tile.height) + mosaic.yOffset)
                    .attr("width", tile.width * data.ratio)
                    .attr("height", tile.height * data.ratio)
                    .attr("vector-effect", "non-scaling-stroke")
                    .style("stroke", "#000")
                    .style("stroke-width", "1")
                    .classed('background', true)
                    .on("mouseover", function () {
                        cleanTmpSvgOverlayElems();
                        drawDimensionH(tile.x, tile.x + tile.width, mosaic);
                        drawDimensionV(tile.y, tile.y + tile.height, mosaic);
                        if (isSvgPannable) {
                            d3.select(this).style("cursor", "move")
                        }
                    })
                    .on("mouseout", function () {
                        cleanTmpSvgOverlayElems();
                        drawBaseDimensions(mosaic);
                        if (isSvgPannable) {
                            d3.select(this).style("cursor", "default")
                        }
                    })
                    .on("click", function (x1, y1, x2, y2) {
                        return function () {
                            //alert('test');
                        };
                    }(tile.x1, tile.y1, tile.x2, tile.y2));

                angular.forEach(gradients, function (gradient, index) {
                    if (tile.requestObjId % gradients.length === index) {
                        rec.attr('fill', tile.final == true ? "url(#" + gradient + ")" : "#f5f5f5")
                    }
                });
        });
    }

    function drawCutAnimation(mosaic) {
        // Loop through all mosaics for this solution
        angular.forEach(mosaic.cuts, function (cut, index) {
            svgContainer.append("line")
                .style("stroke", "black")
                .attr("x1", cut.x1 * data.ratio)
                .attr("y1", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
                .attr("x2", cut.x1 * data.ratio)
                .attr("y2", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
                .style("stroke-width", "2")
                .transition()
                .duration(1500)
                .delay(1500 * index)
                .attr("x1", cut.x1 * data.ratio)
                .attr("y1", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
                .attr("x2", cut.x2 * data.ratio)
                .attr("y2", getTileY2(mosaic.base.height, cut.y2) + mosaic.yOffset);
        });
    };

    var dimensionsSvgElems = [];

    function removeDimensions() {
        angular.forEach(dimensionsSvgElems, function(svg, index) {
            svg.remove();
        });
    }

    function toggleFontSize() {

        cleanTmpSvgOverlayElems();
        drawBaseDimensions();
        removeDimensions();
        showDimensions();
    }

    function showDimensions() {
        angular.forEach(TilingData.getMosaics(), function(mosaic, index) {
            angular.forEach(mosaic.tiles, function (tile, index) {

                if (tile.hasChildren) {
                    return;
                }

                // Only render dimensions text if tile is big enough
                if (tile.width * data.ratio > 20 && tile.height * data.ratio > 20) {

                    // Text size and margin varies inversely as zoom
                    var textMargin = 10 * (1 / zoom);
                    var fontSize = innerFontSize * (1 / zoom) + "px";

                    var textWidth = svgContainer.append("text")
                        .attr("x", (tile.x + tile.width / 2) * data.ratio)
                        .attr("y", getTileY2(mosaic.base.height, tile.y + tile.height) + mosaic.yOffset + textMargin)
                        .attr("fill", "#000000")
                        .attr('text-anchor', 'middle')
                        .style("font-size", fontSize)
                        .text(tile.width);

                    dimensionsSvgElems.push(textWidth);

                    var textWidth = svgContainer.append("text")
                        .attr("fill", "#000000")
                        .attr('text-anchor', 'middle')
                        .style("font-size", fontSize)
                        .text(tile.height)
                        .attr("transform", function (d, i) {
                            return "translate(" + ((tile.x) * data.ratio + textMargin) + " , " + (getTileY2(mosaic.base.height, tile.y + tile.height / 2) + mosaic.yOffset) + ") rotate(270)";
                        });

                    dimensionsSvgElems.push(textWidth);
                }
            });
        });
    };

    function cleanTmpSvgOverlayElems() {
        angular.forEach(tmpSvgOverlayElems, function (svg, index) {
            svg.remove();
        });
    }

    function resetSvgOverlayElems() {
        cleanTmpSvgOverlayElems();
        drawBaseDimensions();
    }

    function getTileY2(rootHeight, y) {
        return (rootHeight - y) * data.ratio;
    };

    function drawDimensionH(x1, x2, mosaic) {

        var textMargin;

        // Text size and margin varies inversely as zoom
        var textMargin = 12 * (1 / zoom);
        var fontSize = 12 * (1 / zoom) + "px";

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", x1 * data.ratio)
            .attr("y1", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset)
            .attr("x2", x2 * data.ratio)
            .attr("y2", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", x1 * data.ratio)
            .attr("y1", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset + 4)
            .attr("x2", x1 * data.ratio)
            .attr("y2", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset - 4);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", x2 * data.ratio)
            .attr("y1", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset + 4)
            .attr("x2", x2 * data.ratio)
            .attr("y2", getTileY2(mosaic.base.height, 0) + mosaic.yOffset + dimensionsOffset - 4);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("text")
            .attr("x", (x1 + ((x2 - x1) / 2)) * data.ratio)
            .attr("y", mosaic.base.height * data.ratio + mosaic.yOffset + dimensionsOffset + textMargin)
            .attr("fill", color1)
            .attr('text-anchor', 'middle')
            .style("font-size", fontSize)
            .text(x2 - x1);

        tmpSvgOverlayElems.push(svgElement);
    };


    var drawDimensionV = function(y1, y2, mosaic) {

        var textMargin;

        // Text size and margin varies inversely as zoom
        var textMargin = 12 * (1 / zoom);
        var fontSize = 12 * (1 / zoom) + "px";

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", mosaic.base.width * data.ratio + dimensionsOffset)
            .attr("y1", getTileY2(mosaic.base.height, y1) + mosaic.yOffset)
            .attr("x2", mosaic.base.width * data.ratio + dimensionsOffset)
            .attr("y2", getTileY2(mosaic.base.height, y2) + mosaic.yOffset);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", mosaic.base.width * data.ratio + dimensionsOffset - 4)
            .attr("y1", getTileY2(mosaic.base.height, y1) + mosaic.yOffset)
            .attr("x2", mosaic.base.width * data.ratio + dimensionsOffset + 4)
            .attr("y2", getTileY2(mosaic.base.height, y1) + mosaic.yOffset);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("line")
            .style("stroke", color1)
            .style("stroke-width", "1")
            .attr("x1", mosaic.base.width * data.ratio + dimensionsOffset - 4)
            .attr("y1", getTileY2(mosaic.base.height, y2) + mosaic.yOffset)
            .attr("x2", mosaic.base.width * data.ratio + dimensionsOffset + 4)
            .attr("y2", getTileY2(mosaic.base.height, y2) + mosaic.yOffset);

        tmpSvgOverlayElems.push(svgElement);

        svgElement = svgContainer.append("text")
            .attr("fill", color1)
            .attr('text-anchor', 'middle')
            .style("font-size", fontSize)
            .text(y2 - y1)
            .attr("transform", function (d, i) {
                return "translate(" + (mosaic.base.width * data.ratio + dimensionsOffset + textMargin) + " , " + (getTileY2(mosaic.base.height, (y1 + ((y2 - y1) / 2))) + mosaic.yOffset) + ") rotate(270)";
            });

        tmpSvgOverlayElems.push(svgElement);
    };

    function drawBaseDimensions() {
        angular.forEach(TilingData.getMosaics(), function(mosaic) {
            drawDimensionV(0, mosaic.base.height, mosaic);
            drawDimensionH(0, mosaic.base.width, mosaic);
        });
    }

    function drawCut(mosaic, cut) {

        cleanTmpSvgOverlayElems();

        var result = mosaic.tiles.filter(function (obj) {
            return obj.id == cut.originalTileId;
        })[0];

        var child1 = mosaic.tiles.filter(function (obj) {
            return obj.id == cut.child1TileId;
        })[0];

        var child2 = mosaic.tiles.filter(function (obj) {
            return obj.id == cut.child2TileId;
        })[0];

        // Overlay to diminish background
        var elem = svgContainer.append("rect")
            .attr("width", '100%')
            .attr("height", '100%')
            .attr('fill', "rgba(255,255,255,0.8)")
            .attr("transform", "translate(-5,-12)");    // Undo initial translation

        tmpSvgOverlayElems.push(elem);

        var elem = svgContainer.append("rect")
            .attr("x", result.x * data.ratio)
            .attr("y", getTileY2(mosaic.base.height, result.y + result.height) + mosaic.yOffset)
            .attr("width", result.width * data.ratio)
            .attr("height", result.height * data.ratio)
            .attr("vector-effect", "non-scaling-stroke")
            .style("stroke", "black")
            .style("stroke-width", "3")
            .attr('fill-opacity', "0.6")
            .attr('fill', "url(#gradient)");

        tmpSvgOverlayElems.push(elem);

        if (child1.final === true) {

            var elem = svgContainer.append("rect")
                .attr("x", child1.x * data.ratio)
                .attr("y", getTileY2(mosaic.base.height, child1.y + child1.height) + mosaic.yOffset)
                .attr("width", child1.width * data.ratio)
                .attr("height", child1.height * data.ratio)
                .attr("vector-effect", "non-scaling-stroke")
                .style("stroke", "black");
            // .style("stroke-width", "3")
            // .attr('fill-opacity', "0.7")
            //.attr('fill', "url(#gradient)");
            elem.transition()
                .duration(cutAnimDuration)
                .style("stroke-width", "3")
                .attr('fill-opacity', "1.7")
                .attr('fill', "#b0bec5");
            tmpSvgOverlayElems.push(elem);
        }


        var elem = svgContainer.append("line")
            .style("stroke", color1)
            .attr("x1", cut.x1 * data.ratio)
            .attr("y1", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
            .attr("x2", cut.x1 * data.ratio)
            .attr("y2", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
            .style("stroke-width", "6");

        elem.transition()
            .duration(cutAnimDuration)
            .attr("x1", cut.x1 * data.ratio)
            .attr("y1", getTileY2(mosaic.base.height, cut.y1) + mosaic.yOffset)
            .attr("x2", cut.x2 * data.ratio)
            .attr("y2", getTileY2(mosaic.base.height, cut.y2) + mosaic.yOffset);

        tmpSvgOverlayElems.push(elem);


        if (isHorizontal(cut)) {
            drawDimensionH(cut.x1, cut.x2, mosaic);
            drawDimensionV(child1.y, child1.y + child1.height, mosaic);
            drawDimensionV(child2.y, child2.y + child2.height, mosaic);
        } else if (isVertical(cut)) {
            drawDimensionV(cut.y1, cut.y2, mosaic);
            drawDimensionH(child1.x, child1.x + child1.width, mosaic);
            drawDimensionH(child2.x, child2.x + child2.width, mosaic);
        }
    }

    function highlightTiles(tile) {

        cleanTmpSvgOverlayElems();

        angular.forEach(TilingData.getMosaics(), function(mosaic, index) {

            var tilesToHighlight = mosaic.tiles.filter(function(obj) {
                return obj.requestObjId === tile.id;
            });

            var drawnDimensions = [];

            angular.forEach(tilesToHighlight, function(tile, index) {
                var rec = svgContainer.append("rect")
                    .attr("x", tile.x * data.ratio)
                    .attr("y", getTileY2(mosaic.base.height, tile.y + tile.height) + mosaic.yOffset)
                    .attr("width", tile.width * data.ratio)
                    .attr("height", tile.height * data.ratio)
                    .attr("vector-effect", "non-scaling-stroke")
                    .style("stroke", "#d9534f")
                    .style("stroke-width", "2")
                    .classed('background', true)
                    .attr('fill', "#000")
                    .attr('fill-opacity', "0.1");

                rec.transition()
                    .duration(0)
                    .attr('fill', "#0000")
                    .attr('fill-opacity', "0.2");

                // TODO: Use some uid for mosaic
                // Render dimension if not previous rendered
                var key = tile.x.toString() + "x" + (tile.x + tile.width).toString() + JSON.stringify(mosaic);
                if (drawnDimensions.indexOf(key) == -1) {
                    drawDimensionH(tile.x, tile.x + tile.width, mosaic);
                    drawnDimensions.push(key);
                }

                var key = tile.y.toString() + "x" + (tile.y + tile.height).toString() + JSON.stringify(mosaic);
                if (drawnDimensions.indexOf(key) == -1) {
                    drawDimensionV(tile.y, tile.y + tile.height, mosaic);
                    drawnDimensions.push(key);
                }

                tmpSvgOverlayElems.push(rec);
            });
        });
    }

    function setZoom(zoom) {
        data.zoom = zoom;
    }

    function getZoom() {
        return data.zoom;
    }

    return {
        clear: clear,
        toggleIsSvgPannable: toggleIsSvgPannable,
        getIsSvgPannable: getIsSvgPannable,
        toggleIsDimensionsVisible: toggleIsDimensionsVisible,
        getIsDimensionsVisible: getIsDimensionsVisible,

        getZoom: getZoom,
        setZoom: setZoom,
        renderTiles: renderTiles,
        drawBaseTile: drawBaseTile,
        showDimensions: showDimensions,
        removeDimensions: removeDimensions,
        drawCutAnimation: drawCutAnimation,
        drawTiles: drawTiles,
        drawDimensionH: drawDimensionH,
        drawDimensionV: drawDimensionV,
        drawBaseDimensions: drawBaseDimensions,
        cleanTmpSvgOverlayElems: cleanTmpSvgOverlayElems,
        resetSvgOverlayElems: resetSvgOverlayElems,
        highlightTiles: highlightTiles,
        init: init,
        drawCut: drawCut,
        toggleFontSize: toggleFontSize
    }
});
