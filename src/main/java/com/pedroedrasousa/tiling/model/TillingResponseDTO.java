package com.pedroedrasousa.tiling.model;

import java.util.ArrayList;
import java.util.List;

public class TillingResponseDTO {

    private String returnCode;

    private long elapsedTime;

    private List<Mosaic> mosaics = new ArrayList<>();

    private List<NoFitTile> noFitTiles = new ArrayList<>();

    private int unusedArea;

    private float hvRatio;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public List<Mosaic> getMosaics() {
        return mosaics;
    }

    public void setMosaics(List<Mosaic> mosaics) {
        this.mosaics = mosaics;
    }

    public List<NoFitTile> getNoFitTiles() {
        return noFitTiles;
    }

    public void setNoFitTiles(List<NoFitTile> noFitTiles) {
        this.noFitTiles = noFitTiles;
    }

    public int getUnusedArea() {
        return unusedArea;
    }

    public void setUnusedArea(int unusedArea) {
        this.unusedArea = unusedArea;
    }

    public float getHvRatio() {
        return hvRatio;
    }

    public void setHvRatio(float hvRatio) {
        this.hvRatio = hvRatio;
    }

    public void addNoFitTile(TileDimensions tileDimensions) {

        boolean incremented = false;

        for (NoFitTile noFitTile : this.noFitTiles) {
            if (noFitTile.getId() == tileDimensions.getId()) {
                noFitTile.setQty(noFitTile.getQty() + 1);
                incremented = true;
            }
        }

        if (!incremented) {
            NoFitTile noFitTile = new NoFitTile();
            noFitTile.setId(tileDimensions.getId());
            noFitTile.setWidth(tileDimensions.getWidth());
            noFitTile.setHeight(tileDimensions.getHeight());
            noFitTile.setQty(1);
            this.noFitTiles.add(noFitTile);
        }
    }

    public static class Mosaic {

        private Tile base;

        private List<Tile> tiles = new ArrayList<>();

        private List<Cut> cuts = new ArrayList<>();

        private int usedArea;

        private float usedAreaRatio;

        private int nbrHorizontal;

        private int nbrVertical;

        private int nbrWasted;

        private int unusedArea;

        private float hvRatio;

        public Tile getBase() {
            return base;
        }

        public void setBase(Tile base) {
            this.base = base;
        }

        public List<Tile> getTiles() {
            return tiles;
        }

        public void setTiles(List<Tile> tiles) {
            this.tiles = tiles;
        }

        public List<Cut> getCuts() {
            return cuts;
        }

        public void setCuts(List<Cut> cuts) {
            this.cuts = cuts;
        }

        public int getUsedArea() {
            return usedArea;
        }

        public void setUsedArea(int usedArea) {
            this.usedArea = usedArea;
        }

        public float getUsedAreaRatio() {
            return usedAreaRatio;
        }

        public void setUsedAreaRatio(float usedAreaRatio) {
            this.usedAreaRatio = usedAreaRatio;
        }

        public int getNbrHorizontal() {
            return nbrHorizontal;
        }

        public void setNbrHorizontal(int nbrHorizontal) {
            this.nbrHorizontal = nbrHorizontal;
        }

        public int getNbrVertical() {
            return nbrVertical;
        }

        public void setNbrVertical(int nbrVertical) {
            this.nbrVertical = nbrVertical;
        }

        public int getNbrWasted() {
            return nbrWasted;
        }

        public void setNbrWasted(int nbrWasted) {
            this.nbrWasted = nbrWasted;
        }

        public float getHvRatio() {
            return hvRatio;
        }

        public void setHvRatio(float hvRatio) {
            this.hvRatio = hvRatio;
        }

        public int getUnusedArea() {
            return unusedArea;
        }

        public void setUnusedArea(int unusedArea) {
            this.unusedArea = unusedArea;
        }
    }

    public static class Tile {

        private int id;

        private int requestObjId;

        private int x;

        private int y;

        private int width;

        private int height;

        private boolean isFinal;

        private boolean hasChildren;

        public Tile(int id, int x, int y, int width, int height) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Tile(com.pedroedrasousa.tiling.model.TileNode tile) {
            this.id = tile.getId();
            this.requestObjId = tile.getExternalId();
            this.x = tile.getX1();
            this.y = tile.getY1();
            this.width = tile.getWidth();
            this.height = tile.getHeight();
            this.setFinal(tile.isFinal());
            this.setHasChildren(tile.hasChildren());
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getRequestObjId() {
            return requestObjId;
        }

        public void setRequestObjId(int requestObjId) {
            this.requestObjId = requestObjId;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        public boolean isHasChildren() {
            return hasChildren;
        }

        public void setHasChildren(boolean hasChildren) {
            this.hasChildren = hasChildren;
        }
    }

    public static class NoFitTile {

        private int id;

        private int width;

        private int height;

        private int qty;

        public NoFitTile() {}

        public NoFitTile(int id, int width, int height, int qty) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.qty = qty;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }
}
