package ru.hh.test.crawler.search;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class BoyerMooreSearchContext {
    final Set<String> result =  ConcurrentHashMap.newKeySet();
    final public CountDownLatch latch;


    public BoyerMooreSearchContext(int filesCount) {
        latch = new CountDownLatch(filesCount);
    }
    public void addResult(String filePath){
        result.add(filePath);
    }
}
