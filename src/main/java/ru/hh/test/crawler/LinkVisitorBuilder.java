package ru.hh.test.crawler;
@FunctionalInterface
public interface LinkVisitorBuilder {
    public LinkVisitor build(String link, Integer currentDepth, CrawlerContext context);
}
