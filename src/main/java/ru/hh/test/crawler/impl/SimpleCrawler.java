package ru.hh.test.crawler.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.config.CrawlerConfig;
import ru.hh.test.crawler.Crawler;
import ru.hh.test.crawler.CrawlerContext;
import ru.hh.test.crawler.LinkVisitorResolver;
import ru.hh.test.crawler.index.IndexWriter;
import ru.hh.test.crawler.wiki.WikiPageConsumer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class SimpleCrawler implements Crawler {

    private final LinkVisitorResolver resolver;
    private final ForkJoinPool mainPool;
    private final static Logger log = LoggerFactory.getLogger(SimpleCrawler.class);

    public SimpleCrawler(LinkVisitorResolver resolver) {
        Preconditions.checkArgument(resolver!=null,"Link Visitor Resolver must be not null");
        this.resolver = resolver;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        availableProcessors = (availableProcessors == 1 ? 2 : availableProcessors);
        log.debug("Crawler initialized with Fork join pool of {} threads",availableProcessors );
        this.mainPool = new ForkJoinPool();
    }

    @Override
    public void crawl(String urlString) {
        try {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(urlString),"Empty url specified");
            log.debug("Start crawling");
            URL url = new URL(urlString);
            URL base = new URL(url.getProtocol(),url.getHost(),url.getPort(),"");
            CrawlerContext context = new SimpleCrawlerContext(base.toString());
            log.debug("Crawler context initialized {}",context);
            log.debug("Invoking root task");
            mainPool.invoke(resolver.resolve(urlString,0,context));
            log.debug("Waiting for termination of all crawling task");
            mainPool.shutdown();
            mainPool.awaitTermination(10, TimeUnit.MINUTES);
            log.debug("All crawling tasks completed");
            log.debug("Waiting for termination of all file writing tasks");
            WikiPageConsumer.getInstance().shutdown();
            log.debug("All fileWriting task tasks completed");
            if (CrawlerConfig.getInstance().useIndex()) {
                log.debug("Crawler configured to use indexing. Waiting for flushing all indexes to disk");
                IndexWriter.getInstance().flushForce();
                log.debug("All indexes writed on disk");
            }
        } catch (MalformedURLException e) {
            log.error("User specified non valid url {}",urlString,e);
            throw new RuntimeException("Not valid url "+urlString, e);

        } catch (InterruptedException e) {
            log.error("Interrupted exception occurred during waiting of shutdown pools ",e);
            throw new RuntimeException(e);
        }


    }
}
