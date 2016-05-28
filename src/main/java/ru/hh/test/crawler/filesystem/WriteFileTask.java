package ru.hh.test.crawler.filesystem;

import ru.hh.test.crawler.Page;
import ru.hh.test.crawler.filesystem.FileWriter;

public class WriteFileTask implements Runnable {
    private final Page page;

    public WriteFileTask(Page page) {
        this.page = page;
    }



    @Override
    public void run() {
        FileWriter.getInstance().writeFile(page);
    }
}
