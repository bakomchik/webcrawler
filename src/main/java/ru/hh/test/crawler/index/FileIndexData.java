package ru.hh.test.crawler.index;

import java.util.Set;

public class FileIndexData {
    public String path;
    public Set<String> dict;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<String> getDict() {
        return dict;
    }

    public void setDict(Set<String> dict) {
        this.dict = dict;
    }
}
