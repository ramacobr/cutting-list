package com.pedroedrasousa.cutlistoptimizer.comparator;

import com.pedroedrasousa.cutlistoptimizer.model.Solution;

import java.util.Comparator;

public class SolutionMostUnusedPanelAreaComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return (int)(o2.getMostUnusedPanelArea() - o1.getMostUnusedPanelArea());
    }
}
