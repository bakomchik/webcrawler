package ru.hh.test.crawler.parser;

import org.jsoup.nodes.Document;
import ru.hh.test.crawler.PageContent;

import java.net.URL;

public interface JsoupPageBuilder {
    public PageContent buildPage(URL url,Document doc);
}
