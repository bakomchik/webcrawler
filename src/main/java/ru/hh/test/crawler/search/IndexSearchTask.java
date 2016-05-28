package ru.hh.test.crawler.search;

import com.google.common.base.Stopwatch;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.suffix.ConcurrentSuffixTree;
import com.googlecode.concurrenttrees.suffix.SuffixTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class IndexSearchTask implements Callable<Set<String>> {
    private final Logger logger = LoggerFactory.getLogger(IndexSearchTask.class);
    private final File indexFile;
    private final File mappingFile;

    private final String searchWord;

    public IndexSearchTask(File indexFile, File mappingFile, String searchWord) {
        this.indexFile = indexFile;
        this.mappingFile = mappingFile;
        this.searchWord = searchWord;
    }

    @Override
    public Set<String> call() throws Exception {
       Iterable<Long> offset = resolveOffsets(searchWord);
        if(offset == null){
            return new HashSet<>();
        }
       try {
         return readFromIndex(offset);
       }
       catch (IOException e){
           logger.warn("Cant read index.file {} ",indexFile.getPath(),e);
       }
       return new HashSet<>();
    }

    private Set<String> readFromIndex(Iterable<Long> offsetsOfSerializedObject) throws IOException, ClassNotFoundException {
        Stopwatch started = Stopwatch.createStarted();
        Set<String> result = new HashSet<>();
        List<ObjectInputStream> streams = new ArrayList<>();
        try( FileInputStream fis = new FileInputStream(indexFile)) {
           for (Long aLong : offsetsOfSerializedObject) {
               fis.getChannel().position(aLong);
               ObjectInputStream objectInputStream = new ObjectInputStream(fis);
               String[] obj = (String[]) objectInputStream.readObject();
               streams.add(objectInputStream);
               if(obj == null){
                   continue;
               }
               result.addAll(Arrays.asList(obj));

           }
       }
        catch (Exception e){
            logger.warn("Cant read from index",e);
        }
        closeStreams(streams);
        logger.info("Index read {} " ,started.elapsed(TimeUnit.MILLISECONDS));
        return  result;

    }

    private void closeStreams(List<ObjectInputStream> streams) {
        for (ObjectInputStream stream : streams) {
            tryCloseStream(stream);
        }
    }

    private void tryCloseStream(ObjectInputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            logger.warn("cant close stream",e);

        }
    }

    private Iterable<Long> resolveOffsets(String searchWord) {
       logger.debug("Starting to resolve index ofssets");
        Stopwatch started = Stopwatch.createStarted();
        try(ObjectInputStream ois = getObjectInputStream(mappingFile)) {
           Map<String,Long> mapping = (Map<String,Long>) ois.readObject();
           logger.debug("Offset index read. Took {}",started.elapsed(TimeUnit.MILLISECONDS));
           return  buildTree(mapping).getValuesForKeysContaining(searchWord);
       } catch (Exception e) {
           logger.warn("IOException occurred when reading mapping file");
           return Collections.emptyList();
       }
    }

    private SuffixTree<Long> buildTree(Map<String, Long> mapping) {
        Stopwatch started = Stopwatch.createStarted();
        logger.debug("Creating suffix tree");

        ConcurrentSuffixTree<Long> tree = new ConcurrentSuffixTree<Long>(new DefaultCharArrayNodeFactory());
        for (String s : mapping.keySet()) {
            tree.put(s,mapping.get(s));
        }
        logger.debug("Build tree completed. Took {}",started.elapsed(TimeUnit.MILLISECONDS));

        return tree;
    }

    private ObjectInputStream getObjectInputStream(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return new ObjectInputStream(fileInputStream);
    }
}
