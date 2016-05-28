package ru.hh.test.crawler.wiki;

import ru.hh.test.crawler.CrawlerContext;
import ru.hh.test.crawler.LinkVisitor;
import ru.hh.test.crawler.LinkVisitorBuilder;

public class WikiLinkVisitorBuilder implements LinkVisitorBuilder {
    @Override
    public LinkVisitor build(String link, Integer currentDepth, CrawlerContext context) {
        return new WikipediaLinkVisitor(link,currentDepth,context);
    }
}
