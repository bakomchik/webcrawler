package ru.hh.test.crawler.wiki;

import org.jsoup.nodes.Document;
import ru.hh.test.crawler.PageContent;
import ru.hh.test.crawler.parser.JSOUPPageContent;
import ru.hh.test.crawler.parser.JsoupPageBuilder;

import java.net.URL;

public class WikiJsoupPageBuilder implements JsoupPageBuilder {
    @Override
    public PageContent buildPage(URL url, Document doc) {
        return JSOUPPageContent.wikiPage(url, doc);
    }
}
