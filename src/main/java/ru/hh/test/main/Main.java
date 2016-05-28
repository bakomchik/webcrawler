package ru.hh.test.main;

import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.config.CrawlerConfig;
import ru.hh.test.config.PropKeys;
import ru.hh.test.crawler.Crawler;
import ru.hh.test.crawler.LinkVisitorBuilder;
import ru.hh.test.crawler.impl.SimpleCrawler;
import ru.hh.test.crawler.impl.SimpleLinkVisitorResolver;
import ru.hh.test.crawler.search.SearchEngine;
import ru.hh.test.crawler.wiki.WikipediaLinkVisitor;

public class Main {
    static Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws ParseException {
        Option crawlOpt = OptionBuilder.hasArgs(1)
                .withArgName("URL which will be root of crawling")
                .withDescription("Recursive fetch link with root specified in args ").isRequired(false)
                .withLongOpt("crawl").create("c");

        Option searchOpt = OptionBuilder.hasArgs(1)
                .withArgName("Word for search")
                .withDescription("Search files which contains the word ").isRequired(false).
                withLongOpt("search").create("s");

        Option configLocation = OptionBuilder.hasArgs(1)
                .withArgName("Valid path to confog file")
                .withDescription("Path to config file.")
                .isRequired(false).
                        withLongOpt("properties").create("p");

        Option useIndex = OptionBuilder
                .withDescription("Use index for search. May be faster with huge amount of files ").isRequired(false).
                        withLongOpt("useindex").create("ui");
        Option help = OptionBuilder
                .withDescription("Prints this message ").isRequired(false).
                        withLongOpt("help").create("h");

        Options o = new Options();
        o.addOption(crawlOpt);
        o.addOption(searchOpt);
        o.addOption(configLocation);
        o.addOption(useIndex);
        o.addOption(help);



        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = parser.parse(o, args);

        if(cmdLine.hasOption("h")){
            new HelpFormatter().printHelp("crawler",o);
        }
        if(cmdLine.hasOption("p")){
            System.setProperty("config.file", cmdLine.getOptionValue("c"));
        }
        CrawlerConfig.getInstance().setBoolProperty(PropKeys.USE_INDEX,cmdLine.hasOption("ui"));


          if(cmdLine.hasOption("c")){
            LinkVisitorBuilder builder = WikipediaLinkVisitor::new;
            Crawler crawler = new SimpleCrawler(SimpleLinkVisitorResolver
                   .builder()
                   .domain("https://ru.wikipedia.org", builder)
                   .domain("https://en.wikipedia.org", builder)
                   .domain("http://en.wikipedia.org", builder)
                   .domain("http://ru.wikipedia.org", builder));
           crawler.crawl(cmdLine.getOptionValue("c"));
        }
        if(cmdLine.hasOption("s")){
            SearchEngine.instance.search(cmdLine.getOptionValue(("s")));
        }




    }
}
