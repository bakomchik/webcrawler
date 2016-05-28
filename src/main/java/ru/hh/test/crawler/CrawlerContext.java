package ru.hh.test.crawler;

public interface CrawlerContext {

    Long nextIndex();

    boolean isVisited(String link);

    boolean tryVisit(String link);


}
