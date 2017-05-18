package spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
