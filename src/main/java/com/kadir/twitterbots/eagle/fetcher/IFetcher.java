package com.kadir.twitterbots.eagle.fetcher;

/**
 * @author akadir
 * Date: 2019-08-09
 * Time: 22:02
 */
public interface IFetcher<T, U> {
    public T get(U... args);
}
