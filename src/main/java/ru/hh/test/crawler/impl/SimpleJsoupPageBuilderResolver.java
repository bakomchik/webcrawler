package ru.hh.test.crawler.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.utils.Utils;
import ru.hh.test.crawler.parser.JsoupPageBuilder;
import ru.hh.test.crawler.parser.JsoupPageBuilderResolver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SimpleJsoupPageBuilderResolver implements JsoupPageBuilderResolver {

    private final Map<String,JsoupPageBuilder> mapping = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(SimpleJsoupPageBuilderResolver.class);


    public static JsoupPageBuilderResolver builder(){
        return new SimpleJsoupPageBuilderResolver();
    }
    private SimpleJsoupPageBuilderResolver(){

    }
    @Override
    public final  JsoupPageBuilder resolveBuilder(String urlString) {
        try {
            log.debug("Mapping : \n{}",mapping);
            log.debug("try to resolve page builder for {}", urlString);
            URL url = new URL(urlString);
            JsoupPageBuilder jsoupPageBuilder = mapping.get(url.toString());

            if(jsoupPageBuilder == null){
                log.debug("Builder not resolved fro exact url {}. Try to resolve by domain",urlString);
                jsoupPageBuilder = mapping.get(Utils.baseUrl(url).toString());
            }
            Preconditions.checkState(jsoupPageBuilder!=null,"No page builder configured for url %s",urlString);
            log.debug("Builder  resolved ",jsoupPageBuilder);
            return jsoupPageBuilder;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Wrong url provided "+urlString,e);
        }
    }



    @Override
    public final JsoupPageBuilderResolver domain(String url, JsoupPageBuilder builder) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),"Url must be not empty or and not null");
        Preconditions.checkArgument(builder!=null,"JsoupPageBuilder must be not  null");
        this.mapping.put(Utils.baseUrl(url).toString(),builder);
        return  this;
    }

    @Override
    public final JsoupPageBuilderResolver exactUrl(String url, JsoupPageBuilder builder) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),"Url must be not empty or and not null");
        Preconditions.checkArgument(builder!=null,"JsoupPageBuilder must be not  null");
        this.mapping.put(Utils.urlSafe(url).toString(),builder);
        return  this;
    }
}
