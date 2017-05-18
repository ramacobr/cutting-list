package com.pedroedrasousa.tiling.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TrackingControler {

    private final static Logger logger = LoggerFactory.getLogger(TrackingControler.class);

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/tracking", method = RequestMethod.GET)
    public void getTaskStatus(HttpServletRequest request) {
        logger.info(request.getRemoteAddr());
    }
}
