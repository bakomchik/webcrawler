package ru.hh.test.crawler.filesystem;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.crawler.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FileWriter {


    private final Set<String> files = ConcurrentHashMap.newKeySet();
    private static volatile FileWriter instance = null;
    private Logger LOG = LoggerFactory.getLogger(FileWriter.class);
    public static FileWriter getInstance() {
        FileWriter localInstance = instance;
        if (localInstance == null) {
            synchronized (FileWriter.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FileWriter();
                }
            }
        }
        return localInstance;
    }

    private FileWriter() {

    }


    public void writeFile(Page page) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!files.add(page.getUniqueStringIdentitfier())) {
            LOG.warn("File {} already processing", page.getPath().toString());
            return;
        }
        Path path = page.getPath();

        LOG.debug("Start to write file {} ", path.toString());
        Path parent = path.getParent();

        try {
            if(!Files.exists(parent)){
                LOG.debug("Parent directory  {}  not exists.Creating...", path.toString());
                Files.createDirectories(parent);
            }
            if (Files.exists(path)) {
                LOG.warn("File {} already exists.", path.toString());
                return ;
            }
            Files.write(path, page.getContent().getText().getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            LOG.info("Write to file {} completed. took{} ", path.toString(),stopwatch.elapsed(TimeUnit.MILLISECONDS));

        } catch (IOException e) {
            LOG.warn("Write to file {} failed. ", path.toString(), e);

        }
    }




}
