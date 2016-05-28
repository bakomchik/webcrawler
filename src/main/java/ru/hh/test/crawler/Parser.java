package ru.hh.test.crawler;

import java.util.Optional;

public interface Parser {

    public Optional<PageContent> parse(String link);
}
