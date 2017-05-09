package com.pedroedrasousa.tiling.spring;

import com.pedroedrasousa.cutlistoptimizer.CutListOptimizerService;
import com.pedroedrasousa.cutlistoptimizer.CutListOptimizerServiceImpl;
import com.pedroedrasousa.cutlistoptimizer.RunningTasks;
import com.pedroedrasousa.cutlistoptimizer.model.Configuration;
import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class CutListOptimizerServiceProxy implements CutListOptimizerService {

    CutListOptimizerService cutListOptimizerService;

    @PostConstruct
    private void init() {
        cutListOptimizerService = CutListOptimizerServiceImpl.getInstance();
    }

    @Override
    public String submitTask(List<TileDimensions> tilesToFit, List<TileDimensions> stockTiles, Configuration cfg) {
        return cutListOptimizerService.submitTask(tilesToFit, stockTiles, cfg);
    }

    @Override
    public RunningTasks.Task getTaskStatus(String taskId) {
        return cutListOptimizerService.getTaskStatus(taskId);
    }

    @Override
    public int stopTask(String taskId) {
        return cutListOptimizerService.stopTask(taskId);
    }
}
