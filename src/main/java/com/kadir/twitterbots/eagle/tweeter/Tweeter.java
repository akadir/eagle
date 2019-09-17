package com.kadir.twitterbots.eagle.tweeter;

import com.kadir.twitterbots.authentication.BotAuthenticator;
import com.kadir.twitterbots.eagle.fetcher.IFetcher;
import com.kadir.twitterbots.eagle.fetcher.YoutubeSubscriberCountFetcher;
import com.kadir.twitterbots.eagle.fetcher.YoutubeSubscriberCountFetcherViaApi;
import com.kadir.twitterbots.eagle.util.EagleConstants;
import com.kadir.twitterbots.exceptions.PropertyNotLoadedException;
import com.kadir.twitterbots.ratelimithandler.handler.RateLimitHandler;
import com.kadir.twitterbots.ratelimithandler.process.ApiProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

/**
 * @author akadir
 * Date: 2019-08-09
 * Time: 22:02
 */
public class Tweeter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IFetcher<Integer, String> youtubeSubscriberCountFetcher;
    private String youtubeAccount;
    private String youtubeAccountLink;
    private String tweetEndsWith;
    private String youtubeAccountId;
    private String youtubeApiKey;
    private Twitter twitter;

    public Tweeter() {
        this.youtubeSubscriberCountFetcher = new YoutubeSubscriberCountFetcherViaApi();
    }

    public void run() {
        readProperties();
        authenticate();
        sendTweet();
    }

    private void authenticate() {
        twitter = BotAuthenticator.authenticate(EagleConstants.PROPERTIES_FILE_NAME, EagleConstants.API_KEYS_PREFIX);
    }

    private void sendTweet() {
        String textToTweet = null;
        Status updatedStatus = null;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("Turkish"));

        try {
            int lastCount = findLastCount();
            int subscriberCount = this.youtubeSubscriberCountFetcher.get(youtubeAccountId, youtubeApiKey);
            int difference = subscriberCount - lastCount;
            logger.info("last count: {} - current count: {} - difference: {}", lastCount, subscriberCount, difference);

            youtubeAccountLink = "https://www.youtube.com/" + youtubeAccount;
            logger.info("set youtubeAccountLink: {}", youtubeAccountLink);
            String tweetEnd = "\n" + youtubeAccountLink + "\n" + tweetEndsWith;
            if (subscriberCount > 0) {
                if (lastCount > 0) {
                    textToTweet = numberFormat.format(subscriberCount) + " abone.\nSon kontrol sonrası değişim: " + ((difference > 0) ? "+" : "") + numberFormat.format(difference) + tweetEnd;
                } else {
                    textToTweet = numberFormat.format(subscriberCount) + " abone." + tweetEnd;
                }
                updatedStatus = twitter.updateStatus(textToTweet);
                logger.info("Status updated to: {}", updatedStatus.getText());
                RateLimitHandler.handle(twitter.hashCode(), updatedStatus.getRateLimitStatus(), ApiProcessType.UPDATE_STATUS);
            } else {
                logger.error("Could not update status");
            }
        } catch (TwitterException e) {
            logger.error("An error occurred while sending tweet: ", e);
        }

    }

    private int findLastCount() {
        try {
            Paging paging = new Paging(1, 100);
            ResponseList<Status> statuses;
            statuses = twitter.getUserTimeline(twitter.getId(), paging);
            RateLimitHandler.handle(twitter.getId(), statuses.getRateLimitStatus(), ApiProcessType.GET_USER_TIMELINE);

            for (Status status : statuses) {
                String lastTweet = status.getText();
                String count = lastTweet.split(" ")[0].replaceAll(",", "").replaceAll("\\.", "");
                try {
                    return Integer.parseInt(count);
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse int from tweet {}: ", status.getText(), e);
                }
            }

        } catch (TwitterException e) {
            logger.error("Error occurred while getting last tweet: ", e);
        }

        return -1;
    }


    private void readProperties() {
        Properties properties = new Properties();

        File propertyFile = new File(EagleConstants.PROPERTIES_FILE_NAME);

        try (InputStream input = new FileInputStream(propertyFile)) {
            properties.load(new InputStreamReader(input, Charset.forName("UTF-8")));
            youtubeAccount = properties.getProperty("youtube-account");
            logger.info("set youtube account: {}", youtubeAccount);
            tweetEndsWith = properties.getProperty("tweet-ends-with");
            logger.info("set tweetEndsWith: {}", tweetEndsWith);
            youtubeAccountId = properties.getProperty("youtube-account-id");
            logger.info("set youtubeAccountId: {}", youtubeAccountId);
            youtubeApiKey = properties.getProperty("youtube-api-key");
            logger.info("set youtubeApiKey: {}", youtubeApiKey);

            logger.info("All properties loaded from file: {}", EagleConstants.PROPERTIES_FILE_NAME);
        } catch (IOException e) {
            logger.error("error occurred while getting properties from file  {} ", EagleConstants.PROPERTIES_FILE_NAME, e);
            throw new PropertyNotLoadedException(EagleConstants.PROPERTIES_FILE_NAME);
        }
    }
}
