package com.pedroedrasousa.cutlistoptimizer.model;

import com.pedroedrasousa.cutlistoptimizer.StockSolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Solution {

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    private final int id;

    private int permutationPriority;

    private long elapsedTime;

    private List<Mosaic> mosaics;

    private List<TileDimensions> noFitTiles;

    public Solution(Solution solution) {
        mosaics = new ArrayList<>();
        for (Mosaic mosaic : solution.mosaics) {
            this.mosaics.add(new Mosaic(mosaic));
        }

        // TileDimensions is immutable, create a shallow copy.
        this.noFitTiles = new ArrayList<>(solution.getNoFitTiles());

        this.permutationPriority = solution.permutationPriority;

        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(StockSolution stockSolution) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        for (TileDimensions tileDimensions : stockSolution.getStockTileDimensions()) {
            this.addMosaic(new Mosaic(tileDimensions));
        }

        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(Solution solution, Mosaic excludeMosaic) {
        mosaics = new ArrayList<>();
        for (Mosaic mosaic : solution.mosaics) {
            if (mosaic != excludeMosaic) {
                this.mosaics.add(new Mosaic(mosaic));
            }
        }

        // TileDimensions is immutable, create a shallow copy.
        this.noFitTiles = new ArrayList<>(solution.getNoFitTiles());

        this.permutationPriority = solution.permutationPriority;
        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(TileNode baseTile) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        mosaics.add(new Mosaic(baseTile));
        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(TileNode... tileNodes) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        for (TileNode tileNode : tileNodes) {
            this.mosaics.add(new Mosaic(tileNode));
        }
        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(TileDimensions tileDimensions) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        mosaics.add(new Mosaic(tileDimensions));
        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(TileDimensions... tileDimensionsList) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        for (TileDimensions tileDimensions : tileDimensionsList) {
            this.addMosaic(new Mosaic(tileDimensions));
        }
        this.id = NEXT_ID.getAndIncrement();
    }

    public Solution(List<TileDimensions> tileDimensionsList) {
        mosaics = new ArrayList<>();
        noFitTiles = new ArrayList<>();
        for (TileDimensions tileDimensions : tileDimensionsList) {
            this.addMosaic(new Mosaic(tileDimensions));
        }
        this.id = NEXT_ID.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public int getPermutationPriority() {
        return permutationPriority;
    }

    public void setPermutationPriority(int permutationPriority) {
        this.permutationPriority = permutationPriority;
    }

    private void sortMosaics() {
        // Java 8
        //Collections.sort(mosaics, Comparator.comparingInt(Mosaic::getUnusedArea));
        Collections.sort(mosaics, new Comparator<Mosaic>() {
            public int compare(Mosaic m1, Mosaic m2) {
                return Integer.compare(m1.getUnusedArea(), m2.getUnusedArea());
            }
        });
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void addMosaic(Mosaic mosaic) {
        this.mosaics.add(mosaic);
        sortMosaics();
    }

    public final List<Mosaic> getMosaics() {
        //return Collections.unmodifiableList(mosaics); TODO: ???
        return mosaics;
    }

    public void removeMosaic(Mosaic mosaic) {
        this.mosaics.remove(mosaic);
    }

    public float getUsedAreaRatio() {
        float ratio = 0;
        for (Mosaic tileNode : mosaics) {
            ratio += tileNode.getRootTileNode().getUsedAreaRatio();
        }
        ratio /= mosaics.size();
        return ratio;
    }

    public boolean hasUnusedBaseTile() {
        for (Mosaic tileNode : mosaics) {
            if (tileNode.getRootTileNode().hasFinal()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int getNbrUnusedTiles() {
        int count = 0;
        for (Mosaic tileNode : mosaics) {
            count += tileNode.getRootTileNode().getNbrUnusedTiles();
        }
        return count;
    }

    public String getBasesAsString() {
        String bases = new String();
        for (Mosaic tileNode : mosaics) {
            bases += "[" + tileNode.getRootTileNode().getWidth() + "x" + tileNode.getRootTileNode().getHeight() + "]";
        }
        return bases;
    }

    public int getNbrHorizontal() {
        int count = 0;
        for (Mosaic tileNode : mosaics) {
            count += tileNode.getRootTileNode().getNbrFinalHorizontal();
        }
        return count;
    }

    public int getNbrFinalTiles() {
        int count = 0;
        for (Mosaic tileNode : mosaics) {
            count += tileNode.getRootTileNode().getNbrFinalTiles();
        }
        return count;
    }

    public float getHVDiff() {
        float ratio = 0;
        for (Mosaic mosaic : mosaics) {
            ratio += mosaic.getHVDiff();
        }
        ratio /= mosaics.size();
        return ratio;
    }

    public int getUsedArea() {
        int usedArea = 0;
        for (Mosaic tileNode : mosaics) {
            usedArea += tileNode.getRootTileNode().getUsedArea();
        }
        return usedArea;
    }

    public int getUnusedArea() {
        int unusedArea = 0;
        for (Mosaic tileNode : mosaics) {
            unusedArea += tileNode.getRootTileNode().getUnusedArea();
        }
        return unusedArea;
    }

    public List<TileDimensions> getNoFitTiles() {
        return noFitTiles;
    }

    public void setNoFitTiles(List<TileDimensions> noFitTiles) {
        this.noFitTiles = noFitTiles;
    }

    public int getMaxDepth() {
        int maxDepth = 0;
        for (Mosaic mosaic : mosaics) {
            maxDepth = Math.max(mosaic.getDepth(), maxDepth);
        }
        return maxDepth;
    }

    public int getNbrCuts() {
        int count = 0;
        for (Mosaic mosaic : mosaics) {
            count += mosaic.getNbrCuts();
        }
        return count;
    }

    public int getDistictTileSet() {
        int discrepancy = 0;
        for (Mosaic mosaic : mosaics) {
            discrepancy = Math.max(mosaic.getDistictTileSet().size(), discrepancy);
        }
        return discrepancy;
    }

    public int getNbrMosaics() {
        return mosaics.size();
    }

    public List<TileDimensions> getStockTilesDimensions() {
        List<TileDimensions> tilesDimensions = new ArrayList<>();
        for (Mosaic mosaic : mosaics) {
            tilesDimensions.add(mosaic.getRootTileNode().toTileDimensions());
        }
        return tilesDimensions;
    }

    public int getMostUnusedPanelArea() {
        int biggestUnusedArea = 0;
        for (Mosaic mosaic : mosaics) {
            if (biggestUnusedArea < mosaic.getUnusedArea()) {
                biggestUnusedArea = mosaic.getUnusedArea();
            }
        }

        return biggestUnusedArea;
    }

    public float getCenterOfMassDistanceToOrigin() {
        float centerOfMassDistToOrigin = 0;
        for (Mosaic mosaic : mosaics) {
            centerOfMassDistToOrigin += mosaic.getCenterOfMassDistanceToOrigin();
        }

        return centerOfMassDistToOrigin / (float)getNbrMosaics();
    }

    public int getBiggestArea() {
        int biggestTileArea = 0;
        for (Mosaic mosaic : mosaics) {
                biggestTileArea = Math.max(mosaic.getBiggestArea(), biggestTileArea);
        }

        return biggestTileArea;
    }
}
