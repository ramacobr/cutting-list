package com.pedroedrasousa.tiling.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Mosaic {

    private final static Logger logger = LoggerFactory.getLogger(Mosaic.class);

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

    public int getUsedArea() {
        return rootTileNode.getUsedArea();
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

    public float getCenterOfMassDistanceToOrigin() {

        if (getUsedArea() == 0) {
            return 0f;
        }

        List<TileNode> finalTiles = getRootTileNode().getFinalTiles();

        float x = 0f, y = 0f;

        for (TileNode tileNode : finalTiles) {
            x += tileNode.getArea() * ((float)tileNode.getX1() + (float)tileNode.getWidth() * 0.5f);
            y += tileNode.getArea() * ((float)tileNode.getY1() + (float)tileNode.getHeight() * 0.5f);
        }

        x = x / getUsedArea();
        y = y / getUsedArea();

        return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / (float)Math.sqrt(Math.pow(getRootTileNode().getWidth(), 2) + Math.pow(getRootTileNode().getHeight(), 2));
    }

    public int getBiggestArea() {
        return rootTileNode.getBiggestArea();
    }
}
