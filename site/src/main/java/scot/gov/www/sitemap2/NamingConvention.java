package scot.gov.www.sitemap2;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.StringUtils.substringAfter;

/**
 * This class encapsulates the naming converntion used for our sitemaps.
 *
 * Each sitemap is for a sub-tree of the content of the site. The path is encoded into the name fo the sitemap, for
 * example:
 *
 * sitemap.policies.xml will contain all content items /content/documents/govscot/policies
 * sitemap.news.2018.01 will contain all content under /content/documents/govscot/news/2019/01
 */
public class NamingConvention {

    /**
     * Turn a jcr path into a sitemap name that can be used in the sitemap index.
     *
     * e.g. /content/documents/govscot/news/2019/01 -> sitemap.news.2019.01.xml
     */
    String requestPathForJcrPath(String path, String contentBasePath) {
        String afterBasePath = StringUtils.substringAfter(path, contentBasePath);
        String dottedPath = afterBasePath.replaceAll("/", ".");
        return String.format("sitemap%s.xml", dottedPath);
    }

    /**
     * Turn the requested url into a jcr path to query for.
     *
     * e.g. /sitemap.news.2018.01.xml -> /content/documents/govscot/news/2019/01
     */
    String jcrPathForRequestPath(String path, String contentBasePath) {
        String stripped = StringUtils.substringBefore(substringAfter(path, "/sitemap"), ".xml");
        String pathVersion = stripped.replaceAll("\\.", "/");
        String encoded = Arrays.stream(pathVersion.split("/")).map(ISO9075::encode).collect(joining("/"));
        return contentBasePath + encoded;
    }

}
