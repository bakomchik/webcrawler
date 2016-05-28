package ru.hh.test.crawler;

import java.net.URL;
import java.util.Set;

public interface PageContent {
    public URL getUrl();
    public String getText();
    public Set<String> getVocabulary();
    public Set<String> getLinks();




}
