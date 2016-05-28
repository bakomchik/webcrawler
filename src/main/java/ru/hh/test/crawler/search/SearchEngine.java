package ru.hh.test.crawler.search;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.config.CrawlerConfig;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class SearchEngine {
    private final Logger logger = LoggerFactory.getLogger(SearchEngine.class);
    public static SearchEngine instance = new SearchEngine();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private SearchEngine(){

    }

    public void search(String searchString) {
        logger.debug("Search statred" );
        Stopwatch started = Stopwatch.createStarted();
        searchString = searchString.trim().toLowerCase();
        Set<String> result;
        if (CrawlerConfig.getInstance().useIndex()) {
            logger.debug("Using index search.");
            result = indexSearch(searchString);

        } else {
            logger.debug("Using Boyer-Moore search.");
            result  = boyerMooreSearch(searchString);
        }
        logger.info("Search completed. took {}",started.elapsed(TimeUnit.MILLISECONDS));
        printResult(result, searchString);
        executorService.shutdown();
        try {
            executorService.awaitTermination(30,TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.warn("Error occurred during waiting for search",e);
        }


    }

    private Set<String> boyerMooreSearch(String searchString) {
        Collection<File> allHtmlsFiles = getAllHtmlsFiles(CrawlerConfig.getInstance().getPathToRepository());
        logger.info("Directory contains {} html files",allHtmlsFiles.size());
        BoyerMooreSearchContext context = new BoyerMooreSearchContext(allHtmlsFiles.size());
        for (File file : allHtmlsFiles) {
            executorService.submit(new BoyerMooreSearchTask(file, searchString, context));
        }
        try {
            logger.debug("All task submitted to executor. Waiting for completion");
            context.latch.await();
            return context.result;


        } catch (InterruptedException e) {
            logger.warn("InterruptedException occurred during search", e);
            return new HashSet<>();
        }
    }

    private Set<String> indexSearch(String searchString) {
        Set<File> pathsToIndexFiles = getPathToIndexFiles();
        if (pathsToIndexFiles.isEmpty()) {
            logger.warn("Index files not found. Fallback to Boyer-More search");
            return boyerMooreSearch(searchString);
        } else {
            return performIndexSearch(pathsToIndexFiles, searchString);
        }
    }

    private Set<String> performIndexSearch(Set<File> pathsToIndexFiles, String searchString) {
        Stopwatch started = Stopwatch.createStarted();
        List<Future<Set<String>>> result = new ArrayList<>();
        for (File file : pathsToIndexFiles) {

            result.add(executorService.submit(new IndexSearchTask(file, getMappingFile(file), searchString)));
        }
        Set<String> foundFiles = new HashSet<>();
        for (Future<Set<String>> setFuture : result) {
            try {
                foundFiles.addAll(setFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Error occured when index search. ", e);
            }
        }
        logger.info("Search took {}", started.elapsed(TimeUnit.MILLISECONDS));
        return foundFiles;

    }

    private File getMappingFile(File file) {
        return new File(file.getPath().replace("idx", "mapping"));
    }

    private void printResult(Set<String> foundFiles, String search) {
        if (foundFiles == null || foundFiles.isEmpty()) {
            logger.info("No file found which contains {}", search);
        } else {
            logger.info(" {} Files found \n{}", foundFiles.size(), Joiner.on("\n").join(foundFiles));
        }
    }

    private Set<File> getPathToIndexFiles() {
        Set<File> indexFiles = new HashSet<>();
        File dir = new File(CrawlerConfig.getInstance().getPathToRepository());
        File[] files = dir.listFiles();
        if (files == null) {
            return indexFiles;
        }
        for (File file : files) {
            if (file.getPath().endsWith(".idx")) {
                indexFiles.add(file);
            }
        }
        return indexFiles;

    }

    public static Collection<File> getAllHtmlsFiles(String directory) {
        return FileUtils.listFiles(
                new File(directory),
                new SuffixFileFilter("html"),
                DirectoryFileFilter.DIRECTORY
        );
    }

}
