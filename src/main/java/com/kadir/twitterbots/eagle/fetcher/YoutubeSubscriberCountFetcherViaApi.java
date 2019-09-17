package com.kadir.twitterbots.eagle.fetcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akadir
 * Date: 17.09.2019
 * Time: 21:53
 */
public class YoutubeSubscriberCountFetcherViaApi implements IFetcher<Integer, String> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Integer get(String... args) {
        try {
            String apiResponse = Jsoup.connect("https://www.googleapis.com/youtube/v3/channels?part=statistics&id=" +
                    args[0] + "&fields=items/statistics/subscriberCount&key=" + args[1])
                    .ignoreContentType(true)
                    .execute()
                    .body();

            JsonObject jsonObject = new JsonParser().parse(apiResponse).getAsJsonObject();
            JsonObject item = jsonObject.getAsJsonArray("items").get(0).getAsJsonObject();
            int subscriberCount = item.get("statistics").getAsJsonObject().get("subscriberCount").getAsInt();

            logger.info("Subscriber count: {}", subscriberCount);
            return subscriberCount;
        } catch (Exception e) {
            logger.error("An error occurred while fetching subscriber count: ", e);
        }

        return 0;
    }
}
