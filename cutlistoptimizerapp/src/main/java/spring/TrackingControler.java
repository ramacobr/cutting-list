package spring;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@RestController
public class TrackingControler {

    private final static Logger logger = LoggerFactory.getLogger(TrackingControler.class);

    @Autowired
    private TrackingService trackingService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/tracking", method = RequestMethod.GET)
    public void getTaskStatus(HttpServletRequest request, @RequestParam(value = "log", required = false) String log) {

        StringBuilder sb = new StringBuilder();

        TrackingDataModel trackingDataModel = null;

            try {
                trackingDataModel = trackingService.getTrackingData(request.getRemoteAddr());
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }

            if (trackingDataModel != null) {

                if (request.getRemoteAddr() != null) {
                    sb.append(request.getRemoteAddr());
                }

                if (trackingDataModel.getCountry() != null) {
                    if (sb.length() != 0) {
                        sb.append(" - ");
                    }
                    sb.append(trackingDataModel.getCountry());
                }

                if (trackingDataModel.getCity() != null) {
                    sb.append(trackingDataModel.getCity());
                }

                if (log != null) {
                    if (sb.length() != 0) {
                        sb.append(" - ");
                    }
                    sb.append(log);
                }
            }

            logger.info(sb.toString());
    }
}
