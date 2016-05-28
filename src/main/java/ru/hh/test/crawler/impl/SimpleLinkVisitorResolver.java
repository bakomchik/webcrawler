package ru.hh.test.crawler.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.utils.Utils;
import ru.hh.test.crawler.CrawlerContext;
import ru.hh.test.crawler.LinkVisitor;
import ru.hh.test.crawler.LinkVisitorBuilder;
import ru.hh.test.crawler.LinkVisitorResolver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SimpleLinkVisitorResolver implements LinkVisitorResolver {

    private final Map<String,LinkVisitorBuilder> mapping = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(SimpleJsoupPageBuilderResolver.class);


    public static LinkVisitorResolver builder(){
        return new SimpleLinkVisitorResolver();
    }
    private SimpleLinkVisitorResolver(){

    }
    @Override
    public final LinkVisitor resolve(String urlString,Integer currentDepth,CrawlerContext context) {
        try {
            log.debug("Mapping : \n{}",mapping);
            log.debug("try to resolve visitor  for {}", urlString);
            URL url = new URL(urlString);
            LinkVisitorBuilder visitor = mapping.get(url.toString());
            if(visitor == null){
                log.debug("visitor not resolved fro exact url {}. Try to resolve by domain",urlString);
                visitor = mapping.get(Utils.baseUrl(url).toString());
            }
            Preconditions.checkState(visitor != null, "No  visitor configured for url %s", urlString);
            log.debug("visitor  resolved ",visitor);
            return visitor.build(urlString,currentDepth,context);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong url provided "+urlString,e);
        }
    }



    @Override
    public final LinkVisitorResolver domain(String url, LinkVisitorBuilder builder) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),"Url must be not empty or and not null");
        Preconditions.checkArgument(builder!=null,"JsoupPageBuilder must be not  null");
        this.mapping.put(Utils.baseUrl(url).toString(),builder);
        return  this;
    }

    @Override
    public final LinkVisitorResolver exactUrl(String url, LinkVisitorBuilder builder) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),"Url must be not empty or and not null");
        Preconditions.checkArgument(builder!=null,"JsoupPageBuilder must be not  null");
        this.mapping.put(Utils.urlSafe(url).toString(),builder);
        return  this;
    }
}
