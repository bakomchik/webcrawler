package ru.hh.test.crawler;

import com.google.common.base.MoreObjects;
import ru.hh.test.utils.Utils;
import ru.hh.test.config.CrawlerConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;


public class Page {
    public final Long id;
    public final String uniqueStringIdentitfier;
    public final String name;
    public final String extension;
    public final String url;
    public final PageContent content;
    public final int depth;
    public final Path path;


    public Page(Long id, String name, String url, PageContent content, int depth) {
        this.id = id;
        this.name = name;
        this.uniqueStringIdentitfier = Utils.replaceNonPrintable(name).trim();
        this.extension = "html";
        this.url = url;
        this.content = content;
        this.depth = depth;
        this.path = resolvePath();
    }

    private Path resolvePath() {
        return Paths.get(
                CrawlerConfig.getInstance().getPathToRepository(),
                Utils.calculateSegment(CrawlerConfig.getInstance().getLoadFactor(), id).toString()
                , name + "." + extension);
    }

    public Path getPath() {
        return path;
    }

    public Long getId() {
        return id;
    }


    public String getUniqueStringIdentitfier() {
        return uniqueStringIdentitfier;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public PageContent getContent() {
        return content;
    }

    public int getDepth() {
        return depth;
    }

    public Set<String> getVocabulary() {
        return content.getVocabulary();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;

        Page page = (Page) o;

        if (!name.equals(page.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uniqueStringIdentitfier", uniqueStringIdentitfier)
                .add("name", name)
                .add("extension", extension)
                .add("url", url)
                .add("content", content)
                .add("depth", depth)
                .add("path", path)
                .toString();
    }
}
