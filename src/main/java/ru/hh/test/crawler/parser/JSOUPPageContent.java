package ru.hh.test.crawler.parser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import ru.hh.test.crawler.wiki.WikiPageContent;
import ru.hh.test.crawler.PageContent;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//FIXME make abstract and create wiki page
public abstract class JSOUPPageContent implements PageContent {

    public static final String UNICODE_NON_WORD_PATTERN = "[^\\p{L}\\p{Nd}]+";
    public static final String WHITESPACE_PATTERN = "\\s+";
    final Document doc;

    final URL url;

    public JSOUPPageContent(URL link,Document doc) {
        this.doc = doc;
        this.url = link;
    }
    public static PageContent wikiPage(URL link,Document doc){
        return new WikiPageContent(link,doc);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getText() {
        return doc.toString();
    }

    @Override
    public Set<String> getVocabulary() {
         Set<String> words  = new HashSet<>();
        new NodeTraversor(new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    String text = textNode.text();
                    if(text.trim().isEmpty()){
                        return;
                    }
                    Set<String> strings = Splitter.onPattern(WHITESPACE_PATTERN)
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(textNode.text())
                            .stream()
                            .map(e-> e.replaceAll(UNICODE_NON_WORD_PATTERN,"").trim().toLowerCase())
                            .filter(str -> !str.isEmpty()).collect(Collectors.toSet());
                    words.addAll(strings);
                }
            }
            public void tail(Node node, int depth) {
            }
        }).traverse(doc.body());
        return words;
    }
    @Override
    public  abstract Set<String> getLinks() ;


    public Document getDoc() {
        return doc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("url", url)
                .toString();
    }
}
