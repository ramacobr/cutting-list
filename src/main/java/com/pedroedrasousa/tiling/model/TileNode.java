package com.pedroedrasousa.tiling.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a tile that can be subdivided.
 *
 * @author Pedro Edra Sousa
 */
public class TileNode {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private int id;

    private int externalId = -1;

    private TileNode child1;

    private TileNode child2;

    private boolean isFinal;

    private Tile tile;

    public TileNode(int x1, int x2, int y1, int y2) {
        this.tile = new Tile(x1, x2, y1, y2);
        this.id = NEXT_ID.getAndIncrement();
    }

    public TileNode(TileDimensions tileDimensions) {
        this.tile = new Tile(tileDimensions);
        this.id = NEXT_ID.getAndIncrement();
    }

    public TileNode(TileNode tileNode) {
        this.tile = tileNode.tile;
        this.id = tileNode.id;
        this.externalId = tileNode.externalId;
        this.isFinal = tileNode.isFinal;

        // Recursively copy children
        if (tileNode.getChild1() != null) {
            this.child1 = tileNode.getChild1();
        }
        if (tileNode.getChild2() != null) {
            this.child2 = tileNode.getChild2();
        }
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TileNode getChild1() {
        return child1;
    }

    public void setChild1(TileNode child1) {
        this.child1 = child1;
    }

    public TileNode getChild2() {
        return child2;
    }

    public void setChild2(TileNode child2) {
        this.child2 = child2;
    }

    public boolean hasChildren() {
        return child1 != null || child2 != null;
    }

    public TileNode findTile(TileNode tile) {

        if (this.equals(tile)) {
            return this;
        }

        if (this.getChild1() != null) {
            TileNode result = this.getChild1().findTile(tile);
            if (result != null) {
                return result;
            }
        }

        if (this.getChild2() != null) {
            return this.getChild2().findTile(tile);
        }

        return null;
    }


    public TileNode replaceTile(TileNode replacement, TileNode tile) {
        if (this.getChild1() != null) {
            TileNode result = this.getChild1().findTile(tile);
            if (result != null) {
                this.setChild1(replacement);
                return this.child1;
            }
        }

        if (this.getChild2() != null) {
            TileNode result = this.getChild2().findTile(tile);
            if (result != null) {
                this.setChild2(replacement);
                return this.child2;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ( !(obj instanceof TileNode) ) {
            return false;
        }

        TileNode tileNode = (TileNode)obj;

        // TODO: No need for this many comparisons
        return this.id == tileNode.id &&
                this.getX1() == tileNode.getX1() &&
                this.getX2() == tileNode.getX2() &&
                this.getY1() == tileNode.getY1() &&
                this.getY2() == tileNode.getY2() &&
                this.isFinal() == tileNode.isFinal() &&
                this.child1 == null && tileNode.child1 == null || (this.child1 != null && this.child1.equals(tileNode.child1)) &&
                this.child2 == null && tileNode.child2 == null || (this.child2 != null && this.child2.equals(tileNode.child2));
    }


    public String toString() {
        return appendToString( "");
    }

    public String appendToString(String trailing) {

        String s = System.getProperty("line.separator") + trailing +  "(" + getX1() + ", " + getY1() + ")(" + getX2() + ", " + getY2() + ")";
        if (isFinal()) {
            s += '*';
        }

        if (child1 != null) {
            trailing += "    ";
            s += child1.appendToString(trailing);
            trailing = trailing.substring(0, trailing.length() - 4);
        }

        if (child2 != null) {
            trailing += "    ";
            s+= trailing + child2.appendToString(trailing);
        }

        return s;
    }


    /**
     * Generates an unique identifier based on tile coordinates and whether is final or not.
     *
     * @return A <code>String</code> representing the identifier.
     */
    public String toStringIdentifier() {
        StringBuilder sb = new StringBuilder();
        appendToStringIdentifier(sb);
        return sb.toString();
    }

    private void appendToStringIdentifier(StringBuilder sb) {

        sb.append(getX1());
        sb.append(getY1());
        sb.append(getX2());
        sb.append(getY2());
        sb.append(isFinal);

        if (child1 != null) {
            child1.appendToStringIdentifier(sb);
        }

        if (child2 != null) {
            child2.appendToStringIdentifier(sb);
        }
    }

    public int getUsedArea() {

        int area = 0;

        if (isFinal()) {
            area += this.getArea();
        }
        if (this.getChild1() != null) {
            area += this.getChild1().getUsedArea();
        }
        if (this.getChild2() != null) {
            area += this.getChild2().getUsedArea();
        }
        return area;
    }

    public List<TileNode> getUnusedTiles() {
        List<TileNode> unusedTiles = new ArrayList<>();
        getUnusedTiles(unusedTiles);
        return unusedTiles;
    }

    public void getUnusedTiles(List<TileNode> unusedTiles) {

        if (!isFinal() && this.getChild1() == null && this.getChild2() == null) {
            unusedTiles.add(this);
        }

        if (this.getChild1() != null) {
            this.getChild1().getUnusedTiles(unusedTiles);
        }

        if (this.getChild2() != null) {
            this.getChild2().getUnusedTiles(unusedTiles);
        }
    }

    public List<TileNode> getFinalTiles() {
        List<TileNode> finalTiles = new ArrayList<>();
        getFinalTiles(finalTiles);
        return finalTiles;
    }

    public void getFinalTiles(List<TileNode> finalTiles) {

        if (isFinal()) {
            finalTiles.add(this);
        }

        if (this.getChild1() != null) {
            this.getChild1().getFinalTiles(finalTiles);
        }

        if (this.getChild2() != null) {
            this.getChild2().getFinalTiles(finalTiles);
        }
    }

    public int getUnusedArea() {
        return getArea() - getUsedArea();
    }

    public float getUsedAreaRatio() {
        return (float)this.getUsedArea() / (float)this.getArea();
    }

    public boolean hasFinal() {
        return (isFinal()) || (child1 != null && child1.hasFinal()) || (child2 != null && child2.hasFinal());
    }

    public int getNbrUnusedTiles() {
        int count = 0;

        if (!this.isFinal() && this.child1 == null && this.child2 == null) {
            count++;
        }

        if (this.child1 != null) {
            count += this.child1.getNbrUnusedTiles();
        }

        if (this.child2 != null) {
            count += this.child2.getNbrUnusedTiles();
        }

        return count;
    }

    public int getDepth() {
        int depth = 0;

        if (this.child1 != null) {
            depth++;
            depth += this.child1.getDepth();
        }

        if (this.child2 != null) {
            depth++;
            depth += this.child2.getDepth();
        }

        return depth;
    }

    public int getNbrFinalTiles() {
        int count = 0;

        if (this.isFinal()) {
            count++;
        }

        if (this.child1 != null) {
            count += this.child1.getNbrFinalTiles();
        }

        if (this.child2 != null) {
            count += this.child2.getNbrFinalTiles();
        }

        return count;
    }


    public int getBiggestArea() {
        int biggestArea = 0;

        if (this.getChild1() == null && this.getChild2() == null && !this.isFinal) {
            biggestArea = this.getArea();
        }

        if (this.child1 != null) {
            biggestArea = Math.max(this.child1.getBiggestArea(), biggestArea);
        }

        if (this.child2 != null) {
            biggestArea = Math.max(this.child2.getBiggestArea(), biggestArea);
        }

        return biggestArea;
    }

    public int getNbrFinalHorizontal() {
        int count = 0;

        if (this.isFinal() && this.isHorizontal()) {
            count++;
        }

        if (this.child1 != null) {
            count += this.child1.getNbrFinalHorizontal();
        }

        if (this.child2 != null) {
            count += this.child2.getNbrFinalHorizontal();
        }

        return count;
    }

    public int getNbrFinalVertical() {
        int count = 0;

        if (this.isFinal() && this.isVertical()) {
            count++;
        }

        if (this.child1 != null) {
            count += this.child1.getNbrFinalVertical();
        }

        if (this.child2 != null) {
            count += this.child2.getNbrFinalVertical();
        }

        return count;
    }

    /**
     * Gets a set of every distinct tile as a string in the format of WxH.
     * Orientation matters.
     * AxB will be considered different from BxA.
     *
     * @return A set of unique strings.
     */
    public HashSet<Integer> getDistictTileSet() {
        HashSet<Integer> set = new HashSet<>();
        return getDistictTileSet(set);
    }

    private HashSet<Integer> getDistictTileSet(HashSet<Integer> set) {

        if (isFinal) {
            int x = tile.getWidth();
            int y = tile.getHeight();
            // Use Cantor pairing function as hash key for performance
            set.add(((x + y)*(x + y + 1)/2) + y);
        } else {

            if (this.child1 != null) {
                this.child1.getDistictTileSet(set);
            }

            if (this.child2 != null) {
                this.child2.getDistictTileSet(set);
            }
        }

        return set;
    }

    public TileDimensions toTileDimensions() {
        return new TileDimensions(getWidth(), getHeight());
    }

    public int getX1() {
        return tile.getX1();
    }

    public int getX2() {
        return tile.getX2();
    }

    public int getY1() {
        return tile.getY1();
    }

    public int getY2() {
        return tile.getY2();
    }

    public int getWidth() {
        return tile.getWidth();
    }

    public int getHeight() {
        return tile.getHeight();
    }

    public int getArea() {
        return getWidth() * getHeight();
    }

    public int getMaxSide() {
        return Math.max(getWidth(), getHeight());
    }

    public boolean isHorizontal() {
        return this.getWidth() > this.getHeight();
    }

    public boolean isVertical() {
        return this.getHeight() > this.getWidth();
    }
}
