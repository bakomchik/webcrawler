package ru.hh.test.crawler.wiki;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.hh.test.crawler.parser.JSOUPPageContent;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class WikiPageContent extends JSOUPPageContent {
    public WikiPageContent(URL link, Document doc) {
        super(link, doc);
    }

    @Override
    public Set<String> getLinks() {
        Elements links = getDoc().select("div#mw-content-text").select("a[href^=/wiki]");
        if (links == null || links.isEmpty()) {
            return Collections.emptySet();
        }
        return links.stream()
                .map(e -> e.attr("href"))
                .filter(e->isNullOrEmpty(FilenameUtils.getExtension(e)))
                .collect(Collectors.toSet());
    }
}
