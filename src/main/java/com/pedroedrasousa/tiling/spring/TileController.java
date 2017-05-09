package com.pedroedrasousa.tiling.spring;

import com.pedroedrasousa.cutlistoptimizer.RunningTasks;
import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;
import com.pedroedrasousa.cutlistoptimizer.model.TillingRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TileController {

    @Autowired
    private CutListOptimizerServiceProxy cutListService;

    private RunningTasks runningTasks = RunningTasks.getInstance();

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/task-status/{taskId}", method = RequestMethod.GET)
    public RunningTasks.Task getTaskStatus(@PathVariable(value="taskId") String taskId) {
        return cutListService.getTaskStatus(taskId);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/stop-task/{taskId}", method = RequestMethod.POST)
    public int stopTask(@PathVariable(value="taskId") String taskId) {
        runningTasks.removeTask(taskId);
        return 0;
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/compute-tilling", method = RequestMethod.POST)
    public String tilling(@RequestBody TillingRequestDTO tilling) {

        List<TileDimensions> tilesToFit = new ArrayList<>();
        List<TileDimensions> stockTiles = new ArrayList<>();

        for (TillingRequestDTO.TileInfoDTO tileInfoDTO : tilling.getTiles()) {
            if (tileInfoDTO.isEnabled() && tileInfoDTO.getWidth() > 0 && tileInfoDTO.getHeight() > 0) {
                for (int i = 0; i < tileInfoDTO.getCount(); i++) {
                    tilesToFit.add(new TileDimensions(tileInfoDTO.getId()/* + (i / 5)*/, tileInfoDTO.getWidth(), tileInfoDTO.getHeight()));
                }
            }
        }

        for (TillingRequestDTO.TileInfoDTO tileInfoDTO : tilling.getBaseTiles()) {
            if (tileInfoDTO.isEnabled() && tileInfoDTO.getWidth() > 0 && tileInfoDTO.getHeight() > 0) {
                for (int i = 0; i < tileInfoDTO.getCount(); i++) {
                    stockTiles.add(new TileDimensions(tileInfoDTO.getId(), tileInfoDTO.getWidth(), tileInfoDTO.getHeight()));
                }
            }
        }

        return cutListService.submitTask(tilesToFit, stockTiles, tilling.getConfiguration());
    }
}
