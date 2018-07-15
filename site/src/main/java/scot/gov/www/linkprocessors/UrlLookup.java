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

    private final String lookupBasePath;

    public UrlLookup(String type) {
        this.lookupBasePath = String.format("/content/urllookups/%s/", type);
    }

    public Node lookupNodeForSlug(Session session, String slug) throws RepositoryException {
        String path = pathForSlug(slug);
        if (session.nodeExists(path)) {
            Node lookup = session.getNode(path);
            return session.getNode(lookup.getProperty("path").getString());
        } else {
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