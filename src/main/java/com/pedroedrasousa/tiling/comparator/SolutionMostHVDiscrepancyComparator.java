package com.pedroedrasousa.tiling.comparator;

import com.pedroedrasousa.tiling.model.Solution;

import java.util.Comparator;


public class SolutionMostHVDiscrepancyComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return (int) ((o1.getDistictTileSet() - o2.getDistictTileSet()) * 1000.0f);
    }
}
