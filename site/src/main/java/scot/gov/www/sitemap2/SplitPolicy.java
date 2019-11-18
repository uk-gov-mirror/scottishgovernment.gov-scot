package scot.gov.www.sitemap2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls which nodes need to be split up into sub index files.
 */
public class SplitPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(SplitPolicy.class);

    Map<String, Integer> depthByType = new HashMap<>();

    private static final int SPLIT_NEWS_BY_YEAR_DEPTH = 6;

    private static final int SPLIT_PUBLICATIONS_BY_MONTH_DEPTH = 7;
    private static final int SPLIT_PUBLICATIONS_BY_YEAR_DEPTH = 6;
    private static final int SPLIT_PUBLICATIONS_BY_TYPE_DEPTH = 5;

    private static final int PUBLICATIONS_TYPE_PATH_INDEX = 5;

    public SplitPolicy() {
        // split these by months simce there are a lot of them
        depthByType.put("advice-and-guidance", SPLIT_PUBLICATIONS_BY_MONTH_DEPTH);
        depthByType.put("foi-eir-release", SPLIT_PUBLICATIONS_BY_MONTH_DEPTH);

        // split these by year
        depthByType.put("consultation-analysis", SPLIT_PUBLICATIONS_BY_YEAR_DEPTH);
        depthByType.put("factsheet", SPLIT_PUBLICATIONS_BY_YEAR_DEPTH);
        depthByType.put("minutes", SPLIT_PUBLICATIONS_BY_YEAR_DEPTH);
        depthByType.put("research-and-analysis", SPLIT_PUBLICATIONS_BY_YEAR_DEPTH);
        depthByType.put("statistics", SPLIT_PUBLICATIONS_BY_YEAR_DEPTH);

        // the rest will be split by type since they are sparse
    }

    boolean shouldSplit(Node node) throws RepositoryException {

        if (isNewsNode(node)) {
            // A news month folder looks like: /content/documents/govscot/news/2019 (i.e. 6 deep)
            return node.getDepth() < SPLIT_NEWS_BY_YEAR_DEPTH;
        }

        if (isPublicationsNode(node)) {
            String type = getPublicationType(node);
            int splitAtDepth = depthByType.getOrDefault(type, SPLIT_PUBLICATIONS_BY_TYPE_DEPTH);
            LOG.info("type is {}, depth is {}", type, splitAtDepth);
            // a publication month folder looks like: /content/documents/govscot/publications/form/2019/01 (i.e. 7 deep)
            return node.getDepth() < splitAtDepth;
        }

        return false;
    }

    String getPublicationType(Node node) throws RepositoryException {
        String [] pathParts = node.getPath().split("/");
        LOG.info("{}, path parts {}", node.getPath(), Arrays.toString(pathParts));
        return node.getDepth() >= PUBLICATIONS_TYPE_PATH_INDEX
                ? node.getPath().split("/")[PUBLICATIONS_TYPE_PATH_INDEX]
                : "";
    }

    boolean isNewsNode(Node node) throws RepositoryException {
        return node.getPath().startsWith("/content/documents/govscot/news");
    }

    boolean isPublicationsNode(Node node) throws RepositoryException {
        return node.getPath().startsWith("/content/documents/govscot/publications");
    }
}
