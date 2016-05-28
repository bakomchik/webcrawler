package ru.hh.test.crawler.parser;

public interface JsoupPageBuilderResolver {

    public JsoupPageBuilder resolveBuilder(String url);
    public JsoupPageBuilderResolver domain(String url,JsoupPageBuilder builder);
    public JsoupPageBuilderResolver exactUrl(String url,JsoupPageBuilder builder);
}
