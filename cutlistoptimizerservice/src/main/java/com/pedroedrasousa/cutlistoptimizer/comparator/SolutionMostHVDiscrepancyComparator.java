package com.pedroedrasousa.cutlistoptimizer.comparator;

import com.pedroedrasousa.cutlistoptimizer.model.Solution;

import java.util.Comparator;


public class SolutionMostHVDiscrepancyComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        // Least distinct tiles sizes meas more tiles with the same orientation
        return (int) ((o1.getDistictTileSet() - o2.getDistictTileSet()) * 1000.0f);
    }
}
