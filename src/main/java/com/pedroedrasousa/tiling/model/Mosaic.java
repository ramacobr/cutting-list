package com.pedroedrasousa.tiling.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Mosaic {

    private TileNode rootTileNode;

    private List<Cut> cuts;

    public Mosaic(Mosaic mosaic) {
        rootTileNode = new TileNode(mosaic.getRootTileNode());
        this.cuts = new ArrayList<>(mosaic.getCuts());
    }

    public Mosaic(TileNode tileNode) {
        cuts = new ArrayList<>();
        rootTileNode = new TileNode(tileNode);
    }

    public Mosaic(TileDimensions tileDimensions) {
        cuts = new ArrayList<>();
        rootTileNode = new TileNode(tileDimensions);
    }

    public TileNode getRootTileNode() {
        return rootTileNode;
    }

    public void setRootTileNode(TileNode rootTileNode) {
        this.rootTileNode = rootTileNode;
    }

    public List<Cut> getCuts() {
        return cuts;
    }

    public void setCuts(List<Cut> cuts) {
        this.cuts = cuts;
    }

    public int getNbrCuts() {
        return cuts.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ( !(obj instanceof Mosaic) ) {
            return false;
        }

        Mosaic mosaic = (Mosaic)obj;

        return mosaic.getRootTileNode().equals(this.getRootTileNode());
    }

    public float getHVDiff() {
        return Math.abs(rootTileNode.getNbrFinalHorizontal() - rootTileNode.getNbrFinalVertical());
    }

    public HashSet<String> getDistictTileSet() {
        return rootTileNode.getDistictTileSet();
    }

    public int getUnusedArea() {
        return rootTileNode.getUnusedArea();
    }

    public int getDepth() {
        return rootTileNode.getDepth();
    }


    public TileNode getBiggestUnusedTile() {

        TileNode biggestUnusedTile = null;
        List<TileNode> unusedTiles = rootTileNode.getUnusedTiles();

        for (TileNode tile : unusedTiles) {
            if (biggestUnusedTile == null || biggestUnusedTile.getArea() < tile.getArea()) {
                biggestUnusedTile = tile;
            }
        }

        return biggestUnusedTile;
    }
}
