package ru.hh.test.crawler.index.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.crawler.index.IndexWriter;
import ru.hh.test.crawler.Page;


public class IndexTask implements Runnable {
    private final Page page;
    private final Logger log = LoggerFactory.getLogger(IndexTask.class);

    public IndexTask(Page page) {
        this.page = page;
    }

    @Override
    public void run() {
        IndexWriter.getInstance().addPageToIndex(page);
    }

}
