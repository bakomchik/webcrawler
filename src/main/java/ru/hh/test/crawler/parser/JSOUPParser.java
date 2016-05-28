package ru.hh.test.crawler.parser;

import com.google.common.base.Stopwatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.crawler.http.HttpService;
import ru.hh.test.crawler.PageContent;
import ru.hh.test.crawler.Parser;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JSOUPParser implements Parser {

    private final Logger log = LoggerFactory.getLogger(JSOUPParser.class);

    private final JsoupPageBuilderResolver resolver;

    public JSOUPParser(JsoupPageBuilderResolver resolver) {
        this.resolver = resolver;
    }


    @Override
    public Optional<PageContent> parse(String s){
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            log.info("Start fetch "+s);
            URL url = new URL(s);
            Optional<String> html = HttpService.getInstance().executeGet(s);
            if(html.isPresent()){
                Document parse = Jsoup.parse(html.get(), "");
                PageContent wrap = resolver.resolveBuilder(s).buildPage(url,parse);
                log.info(s+" fetched took {}",stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return Optional.of(wrap);
            }
            return Optional.empty();

        } catch (IOException e) {
            log.warn("Error occurred when parsing link {}",s,e);
            return Optional.empty();
        }
    }
}
