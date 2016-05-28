package ru.hh.test.crawler.wiki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.crawler.filesystem.WriteFileTask;
import ru.hh.test.config.CrawlerConfig;
import ru.hh.test.crawler.Page;
import ru.hh.test.crawler.index.task.IndexTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WikiPageConsumer implements Consumer<Page> {
    private final Logger log = LoggerFactory.getLogger(WikiPageConsumer.class);
    private static volatile WikiPageConsumer instance = null;

    public static WikiPageConsumer getInstance() {
        WikiPageConsumer localInstance = instance;
        if (localInstance == null) {
            synchronized (WikiPageConsumer.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new WikiPageConsumer();
                }
            }
        }
        return localInstance;
    }

    private WikiPageConsumer() {

    }


    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Runnable createWriteTask(Page page) {
        return new WriteFileTask(page);
    }


    public final void shutdown() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    @Override
    public final void accept(Page page) {
        log.debug("Start consuming page {}", page);
        executor.execute(createWriteTask(page));
        if (CrawlerConfig.getInstance().useIndex()) {
            log.debug("Indexing enabled." );
            executor.execute(createIndexTask(page));
        }
    }

    private Runnable createIndexTask(Page page) {
        return new IndexTask(page);
    }


}
