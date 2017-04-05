package com.pedroedrasousa.tiling.comparator;

import com.pedroedrasousa.tiling.CutListService;
import com.pedroedrasousa.tiling.model.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class SolutionSmallestCenterOfMassDistToOriginComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return (int) ((o1.getCenterOfMassDistanceToOrigin() - o2.getCenterOfMassDistanceToOrigin()) * 100000000.0f);
    }
}
