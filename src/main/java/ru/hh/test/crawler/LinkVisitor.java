package ru.hh.test.crawler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.test.config.CrawlerConfig;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.hh.test.utils.Utils.decode;
import static ru.hh.test.utils.Utils.relative;


public abstract class LinkVisitor extends RecursiveAction {
    private final Logger log = LoggerFactory.getLogger(LinkVisitor.class);
    private final String link;
    private final Integer currentDepth;
    private final CrawlerContext context;
    private final Integer maxDepth;

    public LinkVisitor(String link, Integer currentDepth, CrawlerContext context) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(link),"Empty link received");
        Preconditions.checkArgument(currentDepth!=null,"Null depth received");
        Preconditions.checkArgument(context!=null,"Null CrawlerContext received");
        this.link = link;
        this.currentDepth = currentDepth;
        this.context = context;
        this.maxDepth = CrawlerConfig.getInstance().maxDepth();
    }


    public abstract Parser getParser();

    public abstract Consumer<Page> getConsumer();

private  Parser getParserInternal(){
    Parser parser = getParser();
    log.debug("Current parser is {}",parser );
    return Preconditions.checkNotNull(parser,"Parser can not be null. Pleas override getParser properly");
}
    @Override
    protected void compute() {
        if (currentDepth >= maxDepth) {
            log.debug("Max depth reached.Returning...");
            return;
        }
        if (!tryVisitLink()) {
            log.debug("Ling {} already visited. Skipping....",link);
            return;
        }

        Optional<PageContent> pageContentOptional = getParserInternal().parse(link);
        if (!pageContentOptional.isPresent()) {
            log.warn("Page not obtained. Some errors occurred during parsing or fetchinf from web. See errors above");
            return;
        }
        PageContent page = pageContentOptional.get();
        log.debug("Page fetched. {}",page);
        getConsumerInternal().accept(createPage(page, currentDepth));
        int newDepth = currentDepth + 1;
        if (newDepth > maxDepth) {
            log.debug("Max depth reached.Returning...");
            return;
        }
        Set<LinkVisitor> recursiveTasks = page.getLinks()
                .stream()
                .filter(link -> !context.isVisited(link.trim().toLowerCase()))
                .map(e ->
                        createTask(relative(page.getUrl(), e), context, newDepth)
                )
                .collect(Collectors.toSet());
        log.info("Link {} visited", link);
        if (recursiveTasks.isEmpty()) {
            log.info("No link found on {}", link);
            return;
        }
        log.info("Invoking {} nested links ", recursiveTasks.size());
        invokeAll(recursiveTasks);


    }

    private Consumer<Page> getConsumerInternal() {
        Consumer<Page> consumer = getConsumer();
        log.debug("Consumer is {}",consumer );
        return Preconditions.checkNotNull(consumer,"Consumer can not be null. Pleas override getConsumer properly");
    }

    protected abstract LinkVisitor createTask(String link, CrawlerContext context, int newDepth);


    private boolean tryVisitLink() {
        return context.tryVisit(link.trim().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkVisitor)) return false;

        LinkVisitor that = (LinkVisitor) o;

        return link.equals(that.link);

    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }


    private Page createPage(PageContent page, Integer depth) {
        Long idx = context.nextIndex();
        return new Page(idx, decode(page.getUrl()), page.getUrl().toString(), page, depth);
    }




}
