package com.pedroedrasousa.cutlistoptimizer.comparator;

import com.pedroedrasousa.cutlistoptimizer.model.Solution;

import java.util.Comparator;

public class SolutionSmallestCenterOfMassDistToOriginComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return (int) ((o1.getCenterOfMassDistanceToOrigin() - o2.getCenterOfMassDistanceToOrigin()) * 100000000.0f);
    }
}
