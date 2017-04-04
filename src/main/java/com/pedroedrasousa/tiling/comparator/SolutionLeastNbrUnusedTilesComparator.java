package com.pedroedrasousa.tiling.comparator;

import com.pedroedrasousa.tiling.model.Solution;

import java.util.Comparator;

public class SolutionLeastNbrUnusedTilesComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return o1.getNbrUnusedTiles() - o2.getNbrUnusedTiles();
    }
}
