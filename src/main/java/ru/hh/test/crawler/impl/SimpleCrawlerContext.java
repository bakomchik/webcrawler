package ru.hh.test.crawler.impl;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.crawler.CrawlerContext;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Context for crawling web pages
 * Provides meta information about visited pages
 */
public class SimpleCrawlerContext implements CrawlerContext {
    private final Logger log = LoggerFactory.getLogger(SimpleCrawlerContext.class);
    public SimpleCrawlerContext(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    private final Set<String> visited = ConcurrentHashMap.newKeySet();
    private final AtomicLong linkCounter = new AtomicLong();

    private final String baseUrl;

    @Override
    public Long nextIndex(){
        return linkCounter.incrementAndGet();
    }

    @Override
    public boolean isVisited(String link) {
        boolean linkVisited = visited.contains(link);
        log.debug("{} visited {}",link,linkVisited );
        return linkVisited;
    }


    @Override
    public boolean tryVisit(String link) {
        boolean result = visited.add(link);
        log.debug("{} try visit {}",link,result );
        return result;

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("visited", visited)
                .add("linkCounter", linkCounter)
                .add("baseUrl", baseUrl)
                .toString();
    }
}
