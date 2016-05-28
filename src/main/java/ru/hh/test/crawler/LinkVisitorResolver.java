package ru.hh.test.crawler;

public interface LinkVisitorResolver {
    public LinkVisitor resolve(String urlString,Integer currentDepth,CrawlerContext context);

    LinkVisitorResolver domain(String url,LinkVisitorBuilder builder);

    public  LinkVisitorResolver exactUrl(String url, LinkVisitorBuilder builder) ;
}
