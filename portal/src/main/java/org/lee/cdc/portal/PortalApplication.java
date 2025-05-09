package org.lee.cdc.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortalApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortalApplication.class);

    public static void main(String[] args) {
        LOGGER.info("PortalApplication start");
    }


}
