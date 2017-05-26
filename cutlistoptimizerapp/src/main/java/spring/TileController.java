package spring;

import com.pedroedrasousa.cutlistoptimizer.RunningTasks;
import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;
import com.pedroedrasousa.cutlistoptimizer.model.TillingRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TileController {

    private final static Logger logger = LoggerFactory.getLogger(TileController.class);

    @Autowired
    private CutListOptimizerServiceProxy cutListService;

    @Autowired
    private TrackingService trackingService;

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
    public String tilling(@RequestBody TillingRequestDTO tilling, HttpServletRequest request) {

        List<TileDimensions> tilesToFit = new ArrayList<>();
        List<TileDimensions> stockTiles = new ArrayList<>();

        TrackingDataModel trackingDataModel = trackingService.getTrackingData("89.115.133.189");
        logger.info("Request origin: " + trackingDataModel.getCountry() + " - " + trackingDataModel.getCity() + " - " + trackingDataModel.getOrganisation());

        for (TillingRequestDTO.TileInfoDTO tileInfoDTO : tilling.getTiles()) {
            if (tileInfoDTO.isEnabled() && tileInfoDTO.getWidth() > 0 && tileInfoDTO.getHeight() > 0) {
                for (int i = 0; i < tileInfoDTO.getCount(); i++) {
                    tilesToFit.add(new TileDimensions(tileInfoDTO.getId(), tileInfoDTO.getWidth(), tileInfoDTO.getHeight()));
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
