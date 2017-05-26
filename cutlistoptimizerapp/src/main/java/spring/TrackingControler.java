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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public void getTaskStatus(HttpServletRequest request) {

        logger.info(request.getRemoteAddr());
            TrackingDataModel trackingDataModel = trackingService.getTrackingData(request.getRemoteAddr());
            logger.info(trackingDataModel.getCountry() + " " + trackingDataModel.getCity());
    }
}
