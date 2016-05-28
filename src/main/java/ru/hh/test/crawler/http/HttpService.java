package ru.hh.test.crawler.http;

import com.google.common.base.Stopwatch;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HttpService {
        private final Logger log = LoggerFactory.getLogger(HttpService.class);
        private final HttpClient client;
        private static volatile  HttpService instance = null;

            public static HttpService getInstance() {
             HttpService localInstance = instance;
                if (localInstance == null) {
                    synchronized (HttpService.class) {
                        localInstance = instance;
                        if (localInstance == null) {
                            instance = localInstance = new HttpService();
                        }
                    }
                }
                return localInstance;
            }
            private HttpService() {
                RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5000).setSocketTimeout(5000).build();
                client = HttpClientBuilder.create()
                        .setDefaultRequestConfig(config).setMaxConnTotal(200).setMaxConnPerRoute(100).build();
            }

    public Optional<String> executeGet(String url){
        HttpGet get = new HttpGet(url);
        Stopwatch started = Stopwatch.createStarted();
        log.info("Start Fetch from url {}",url);
        try {
            HttpResponse response = client.execute(get);
            Optional<String> pageBody = null;
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                pageBody = Optional.of(EntityUtils.toString(response.getEntity()));
            }
            else{
                pageBody = Optional.empty();
                EntityUtils.consumeQuietly(response.getEntity());
            }
            log.info("{} fetched took {}",url,started.elapsed(TimeUnit.MILLISECONDS));

            return pageBody;
        } catch (IOException e) {
            log.error("Error occurred during http call to {} ",url,e);
            return Optional.empty();

        }
        finally {
            get.releaseConnection();
        }
    }

}
