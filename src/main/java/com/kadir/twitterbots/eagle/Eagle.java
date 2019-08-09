package com.kadir.twitterbots.eagle;

import com.kadir.twitterbots.eagle.tweeter.Tweeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akadir
 * Date: 2019-08-09
 * Time: 21:58
 */
public class Eagle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        Eagle eagle = new Eagle();
        eagle.start();
    }

    private void start() {
        Tweeter tweeter = new Tweeter();
        logger.info("start tweeting");
        tweeter.run();
    }
}
