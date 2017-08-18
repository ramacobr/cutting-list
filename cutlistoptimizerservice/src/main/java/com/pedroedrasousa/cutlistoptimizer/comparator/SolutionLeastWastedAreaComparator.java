package com.pedroedrasousa.cutlistoptimizer.comparator;

import com.pedroedrasousa.cutlistoptimizer.model.Solution;

import java.util.Comparator;

public class SolutionLeastWastedAreaComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return (int)(o1.getUnusedArea() - o2.getUnusedArea());
    }
}
