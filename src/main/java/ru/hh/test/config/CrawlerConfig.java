package ru.hh.test.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Strings.*;

public class CrawlerConfig {
    public static final String CRAWLER_PROPERTIES_FILE_NAME = "crawler.properties";
    private final Properties properties;
    private static Logger log = LoggerFactory.getLogger(CrawlerConfig.class);


    private static volatile  CrawlerConfig instance = null;

    public static CrawlerConfig getInstance() {
        CrawlerConfig localInstance = instance;
        if (localInstance == null) {
            synchronized (CrawlerConfig.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CrawlerConfig();
                }
            }
        }
        return localInstance;
    }
    private CrawlerConfig() {
        this.properties = new Properties();
        try (InputStream propertiesStream = openStream(System.getProperty("config.file"))) {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProperty(String key,String value){
        properties.put(key,value);
    }
    public void setBoolProperty(String key,Boolean value){
        setProperty(key,value.toString());
    }



    private InputStream openStream(String configPath) throws FileNotFoundException {
        return isNullOrEmpty(configPath)
                ? inputStreamFromClassPath()
                : inputStreamFromFile(configPath);

    }

    public boolean useIndex(){
        return getBoolDefault(PropKeys.USE_INDEX,false);
    }

    private FileInputStream inputStreamFromFile(String configPath) throws FileNotFoundException {
        Preconditions.checkArgument(Strings.isNullOrEmpty(configPath), "Empty config file path specified");
        return new FileInputStream(configPath);
    }

    private InputStream inputStreamFromClassPath() throws FileNotFoundException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(CRAWLER_PROPERTIES_FILE_NAME);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Cant instantiate crawler.Default config not provided but " +
                    "external config source not specified. ");
        }
        return resourceAsStream;
    }

    public int getMaxThreads() {
        return getIntDefault(PropKeys.MAX_THREADS, 64);
    }

    public Integer getIntDefault(String key, Integer defaultValue){
        String property = properties.getProperty(key);
        if (Strings.isNullOrEmpty(property)){
            return defaultValue;
        }
        return Integer.parseInt(property);

    }
    public boolean getBoolDefault(String key, Boolean defaultValue){
        String property = properties.getProperty(key);
        if (Strings.isNullOrEmpty(property)){
            return defaultValue;
        }
        return Boolean.parseBoolean(property);

    }
    public Long getLongDefault(String key, Long defaultValue){
        String property = properties.getProperty(key);
        if (Strings.isNullOrEmpty(property)){
            return defaultValue;
        }
        return Long.parseLong(property);

    }

    public String getPathToRepository() {
        // FIXME use default
        return  getStringRequired(PropKeys.REPO_PATH);
    }
    public String getStringRequired(String prop){
        String value = properties.getProperty(prop);
        Preconditions.checkState(!Strings.isNullOrEmpty(value),"Property %s not specified",prop);
        return value;
    }

    public Integer maxDepth() {
        return getIntDefault(PropKeys.MAX_DEPTH, 2);
    }

    public Long getLoadFactor() {
        return getLongDefault(PropKeys.LOAD_FACTOR,1000L);
    }

    public Long getIndexLoadFactor() {
        return getLongDefault(PropKeys.INDEX_LOAD_FACTOR,100L);
    }
}
