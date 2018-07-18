package scot.gov.www.linkprocessors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Lookup path for a slug using the urllookups data in jcr
 */
public class UrlLookup {

    private static final Logger LOG = LoggerFactory.getLogger(UrlLookup.class);

    private final String lookupBasePath;

    public UrlLookup(String type) {
        this.lookupBasePath = String.format("/content/urllookups/%s/", type);
    }

    public Node lookupNodeForSlug(Session session, String slug) throws RepositoryException {
        String path = pathForSlug(slug);

        try {
            Node lookup = session.getNode(path);
            String lookupPath = lookup.getProperty("path").getString();
            return session.getNode(lookupPath);
        } catch (RepositoryException e) {
            LOG.warn("No url lookup for {}", slug, e);
            return null;
        }
    }

    private String pathForSlug(String slug) {
        // form the letters of the slug into a path e.g. myslug -> /m/y/s/l/u/g/
        StringBuilder pathBuilder = new StringBuilder(lookupBasePath);
        for(char c : slug.toCharArray()) {
            pathBuilder.append(c).append("/");
        }
        return pathBuilder.toString();
    }

}