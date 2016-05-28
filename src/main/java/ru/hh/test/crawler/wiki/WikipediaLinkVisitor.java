package ru.hh.test.crawler.wiki;

import ru.hh.test.crawler.impl.SimpleJsoupPageBuilderResolver;
import ru.hh.test.crawler.CrawlerContext;
import ru.hh.test.crawler.LinkVisitor;
import ru.hh.test.crawler.Page;
import ru.hh.test.crawler.Parser;
import ru.hh.test.crawler.parser.JSOUPParser;

import java.util.function.Consumer;

public class WikipediaLinkVisitor extends LinkVisitor {
    public WikipediaLinkVisitor(String link, Integer currentDepth, CrawlerContext context) {
        super(link, currentDepth, context);
    }

    @Override
    public Parser getParser() {
        WikiJsoupPageBuilder builder = new WikiJsoupPageBuilder();
        return new JSOUPParser(SimpleJsoupPageBuilderResolver.builder()
                .domain("https://ru.wikipedia.org", builder)
                .domain("https://en.wikipedia.org", builder)
                .domain("http://en.wikipedia.org", builder)
                .domain("http://ru.wikipedia.org", builder)

        );
    }

    @Override
    public Consumer<Page> getConsumer() {
       return WikiPageConsumer.getInstance();
    }

    @Override
    protected LinkVisitor createTask(String link,CrawlerContext context, int newDepth) {
               return new WikipediaLinkVisitor(link, newDepth,context );

    }
}
