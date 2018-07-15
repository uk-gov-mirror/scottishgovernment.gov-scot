package scot.gov.www.linkprocessors;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.linking.HstLinkProcessorTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class NewsLinkProcessor extends HstLinkProcessorTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(NewsLinkProcessor.class);

    public static final String NEWS = "news/";

    UrlLookup urlLookup = new UrlLookup("news");

    @Override
    protected HstLink doPostProcess(HstLink link) {
        if (isNewsFullLink(link)) {
            // remove the date and time...
            link.setPath(String.format("news/%s", link.getPathElements()[3]));
        }

        return link;
    }

    private boolean isNewsFullLink(HstLink link) {
        return link.getPath().startsWith(NEWS) && link.getPathElements().length == 4;
    }

    @Override
    protected HstLink doPreProcess(HstLink link) {
        if (isNewsSlugLink(link)) {
            return preProcessNewsLink(link);
        }
        return link;
    }

    private boolean isNewsSlugLink(HstLink link) {
        return link.getPath().startsWith(NEWS) && link.getPathElements().length == 2;
    }

    private HstLink preProcessNewsLink(HstLink link) {
        try {
            String slug = link.getPathElements()[link.getPathElements().length - 1];

            Session session = RequestContextProvider.get().getSession();
            Node handle = urlLookup.lookupNodeForSlug(session, slug);
            if (handle == null) {
                return link;
            }
            String newPath = String.format("news/%s", StringUtils.substringAfter(handle.getPath(), NEWS));
            link.setPath(newPath);
            return link;
        } catch (RepositoryException e) {
            LOG.warn("Exception trying to imageprocessing link: {}", link.getPath(), e);
            return link;
        }
    }
}