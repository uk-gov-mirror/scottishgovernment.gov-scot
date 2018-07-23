package scot.gov.www.linkprocessors;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.linking.HstLinkProcessorTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class PublicationLinkProcessorUrlLookupImpl extends HstLinkProcessorTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(PublicationLinkProcessorUrlLookupImpl.class);

    public static final String PUBLICATIONS = "publications/";

    private UrlLookup urlLookup = new UrlLookup("publications");

    private static final MetricRegistry metrics = new MetricRegistry();

    private final Timer times = metrics.timer("linktimes");

    static {
        ScheduledReporter reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger("scot.gov.www.linkprocessors.PublicationLinkProcessorUrlLookupImpl"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(30, SECONDS);
    }
    @Override
    protected HstLink doPostProcess(HstLink link) {
        if (isPublicationsFullLink(link)) {
            // remove the type, year and month...
            String [] newElements = ArrayUtils.removeElements(link.getPathElements(),
                    link.getPathElements()[1],
                    link.getPathElements()[2],
                    link.getPathElements()[3]
                    );
            if (link.getPathElements().length > 5 && !"pages".equals(link.getPathElements()[5])) {
                newElements = ArrayUtils.removeElements(newElements, link.getPathElements()[5]);
            }
            link.setPath(String.join("/", newElements));
        }
        return link;
    }

    private boolean isPublicationsFullLink(HstLink link) {
        // should match on any publication or publication page link.
        return link.getPath().startsWith(PUBLICATIONS) && link.getPathElements().length >= 5;
    }

    @Override
    protected HstLink doPreProcess(HstLink link) {
        if (isPublicationsSlugLink(link)) {
            final Timer.Context context = times.time();
            HstLink newlink = preProcessPublicationsLink(link);
            context.stop();
            return newlink;
        }
        return link;
    }

    private boolean isPublicationsSlugLink(HstLink link) {
        // match any slug style link for a publication or page
        return link.getPath().startsWith(PUBLICATIONS) && link.getPathElements().length >= 2;
    }

    private HstLink preProcessPublicationsLink(HstLink link) {

        try {
            String slug = link.getPathElements()[1];
            String [] remaining = ArrayUtils.removeElements(link.getPathElements(),
                    link.getPathElements()[0],
                    link.getPathElements()[1]);
            Session session = RequestContextProvider.get().getSession();
            Node handle = urlLookup.lookupNodeForSlug(session, slug);
            if(handle == null) {
                link.setNotFound(true);
                link.setPath("/pagenotfound");
                return link;
            }

            String pubPath = StringUtils.substringAfter(handle.getPath(), PUBLICATIONS);

            String newPath = null;
            if (remaining.length == 0) {
                // this is a link to the publication, include the /index part.
                newPath = String.format("publications/%s", pubPath);
            } else {
                // this is a link to a page, remove the index and add on the pages part
                newPath = String.format("publications/%s/%s",
                        StringUtils.substringBefore(pubPath, "/index"),
                        String.join("/", remaining));
            }
            link.setPath(newPath);
            return link;
        } catch (RepositoryException e) {
            LOG.warn("Exception trying to imageprocessing link: {}", link.getPath(), e);
            return link;
        }
    }

}