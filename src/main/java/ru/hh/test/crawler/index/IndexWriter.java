package ru.hh.test.crawler.index;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.config.CrawlerConfig;
import ru.hh.test.crawler.Page;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IndexWriter {

    private Set<Page> buffer = ConcurrentHashMap.newKeySet();
    Long indexId = 1L;
    final Lock lock = new ReentrantLock();
    private volatile boolean flushed = false;
    private final Long loadFactor;
    ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    static  final Logger log = LoggerFactory.getLogger(IndexWriter.class);


    private IndexWriter(){
        this.loadFactor = CrawlerConfig.getInstance().getIndexLoadFactor();
    }
        private static volatile IndexWriter instance;

        public static IndexWriter getInstance() {
            IndexWriter localInstance = instance;
            if (localInstance == null) {
                synchronized (IndexWriter.class) {
                    localInstance = instance;
                    if (localInstance == null) {
                        instance = localInstance = new IndexWriter();
                    }
                }
            }
            return localInstance;
        }




    public void addPageToIndex(Page page) {
        Stopwatch started = Stopwatch.createStarted();
        lock.lock();

        Set<Page> bufferLocal = null;
        Long id= null;
        try{
            Preconditions.checkState(!flushed,"Index already flushed");
            buffer.add(page);
            if (buffer.size()>=loadFactor){
                log.debug("Index loadfactor reached. Flushing index to disk");
                id = indexId;
                indexId++;
                bufferLocal = buffer;
                buffer = ConcurrentHashMap.newKeySet();
            }

        }
        finally {
            log.debug("Page {} added to index. took {}", page.getName(), started.elapsed(TimeUnit.MILLISECONDS));
            lock.unlock();
        }
        if(bufferLocal!=null){
            writeIndex(id,bufferLocal);
        }
    }

    private void writeIndex(Long id ,Set<Page> buffer) {

        e.execute(() -> {
            Stopwatch started = Stopwatch.createStarted();
            log.debug("Creating index.");
            Map<String,Set<String>> index = new HashMap<>();

            for (Page page : buffer) {
                for (String o : page.getVocabulary()){
                    index.compute(o, (s, paths) -> {
                        if(paths == null){
                            paths = new HashSet<String>();
                        }
                        paths.add(page.getPath().toString());
                        return paths;
                    });
                }
            }
            log.info("Index created  took {}", started.elapsed(TimeUnit.MILLISECONDS));

            write(id,index);
        });


    }

    private void write(Long id,Map<String,Set<String>> index) {
        Stopwatch started = Stopwatch.createStarted();
        log.info("Start to write index to disk",started.elapsed(TimeUnit.MILLISECONDS));

        Map<String,Long> offsetMap = new HashMap<>();
        long offset = 0;
        try (FileOutputStream indexFile = openStream(getIndexPath(id))){


            for (String s : index.keySet()) {
                offsetMap.put(s,offset);
                Set<String> set = index.get(s);
                String[] strings = set.toArray(new String[set.size()]);
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

                objectOutputStream.writeObject(strings);
                objectOutputStream.flush();
                objectOutputStream.close();

                byte[] bytes = byteOutputStream.toByteArray();
                offset +=bytes.length;
                indexFile.write(bytes);
                indexFile.flush();


            }
            indexFile.close();
        }
        catch (IOException e) {
            log.warn("Cant write index {} ",id,e);
        }
        log.debug("Main Index writed {}", started.elapsed(TimeUnit.MILLISECONDS));
        started.reset();
        try(ObjectOutputStream mapFile = openMappingStream(getMappingFile(id))){
            mapFile.writeObject(offsetMap);
            mapFile.flush();
            mapFile.close();
            log.debug("Mapping file writed {}", started.elapsed(TimeUnit.MILLISECONDS));

        }

        catch (IOException e) {
            log.warn("Cant write maping file {} ",id,e);
        }

    }

    private static ObjectOutputStream openMappingStream(File mappingFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(mappingFile);
        return new ObjectOutputStream(fileOutputStream);
    }

    private static File getMappingFile(Long id) {
        return Paths.get(CrawlerConfig.getInstance().getPathToRepository(),"index_"+id+".mapping").toFile();
    }


    private static FileOutputStream openStream(File file) throws IOException {
        return new FileOutputStream(file);
    }

    private static File getIndexPath(Long id) {
        return Paths.get(CrawlerConfig.getInstance().getPathToRepository(),"index_"+id+".idx").toFile();
    }

    public void flushForce() throws InterruptedException {
        if(flushed){
            return;
        }
        flushed = true;
        lock.lock();
        try{
            Set<Page> buffer1 = buffer;
            buffer = new HashSet<>();
            long id = indexId;
            indexId++;
            writeIndex(id, buffer1);

        }
        finally {
            lock.unlock();
            e.shutdown();
            e.awaitTermination(10,TimeUnit.MINUTES);
        }

    }




}
