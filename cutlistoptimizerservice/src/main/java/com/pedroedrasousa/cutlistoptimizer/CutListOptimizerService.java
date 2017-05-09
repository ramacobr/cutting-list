package com.pedroedrasousa.cutlistoptimizer;

import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;
import com.pedroedrasousa.cutlistoptimizer.model.Configuration;

import java.util.List;


public interface CutListOptimizerService {

    String submitTask(List<TileDimensions> tilesToFit, List<TileDimensions> stockTiles, Configuration cfg);

    RunningTasks.Task getTaskStatus(String taskId);

    int stopTask(String taskId);
}
