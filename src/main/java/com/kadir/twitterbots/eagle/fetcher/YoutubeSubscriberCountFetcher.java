package com.kadir.twitterbots.eagle.fetcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akadir
 * Date: 2019-08-09
 * Time: 22:05
 */
public class YoutubeSubscriberCountFetcher implements IFetcher<Integer, String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Integer get(String... args) {
        Document document = null;
        try {
            document = Jsoup.connect("https://www.youtube.com/" + args[0]).get();
            Elements elements = document.select("span.yt-subscription-button-subscriber-count-branded-horizontal.subscribed.yt-uix-tooltip");
            logger.info("Subscriber count: {}", elements.text());
            return Integer.parseInt(elements.text().replace(".", ""));
        } catch (Exception e) {
            logger.error("An error occurred while fetching subscriber count: ", e);
        }

        return 0;
    }
}
