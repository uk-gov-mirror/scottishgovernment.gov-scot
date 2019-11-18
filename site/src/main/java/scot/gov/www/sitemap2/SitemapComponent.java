package scot.gov.www.sitemap2;

import org.apache.commons.lang.time.StopWatch;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.onehippo.forge.sitemap.components.model.Url;
import org.onehippo.forge.sitemap.components.model.Urlset;
import org.onehippo.forge.sitemap.generator.SitemapGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Component that produces a sitemap for a part of the content tree.  Uses code from the bloomreach forge plugin to
 * produce the XML.
 */
public class SitemapComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SitemapComponent.class);

    // we limit the number of result for performance reasons.
    private static final int MAX_ITEMS_PER_SITEMAP = 1500;

    private static NamingConvention namingConvention = new NamingConvention();

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);

        LOG.info("Generating sitemap for {}", request.getPathInfo());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Urlset urlset = generateSitemap(request);
            request.setAttribute("sitemap", SitemapGenerator.toString(urlset));
            LOG.info("Finished generating sitemap for {}, {} pages, took {} millis",
                    request.getPathInfo(), urlset.getUrls().size(), stopWatch.getTime());
        } catch (RepositoryException e) {
            LOG.error("Failed top generate sitemap", e);
            throw new HstComponentException(e);
        }
    }

    private static Urlset generateSitemap(HstRequest request) throws RepositoryException {
        NodeIterator it = getPublishedNodesForRequest(request);
        return getUrlSetForResults(it, request);
    }

    private static NodeIterator getPublishedNodesForRequest(HstRequest request) throws RepositoryException {
        Session session = request.getRequestContext().getSession();
        String jcrPath = repoPath(request);
        String xpath = xpath(jcrPath);
        Query query = session.getWorkspace().getQueryManager().createQuery(xpath, Query.XPATH);
        query.setLimit(MAX_ITEMS_PER_SITEMAP);
        QueryResult result = query.execute();
        return result.getNodes();
    }

    private static String repoPath(HstRequest request) {
        HstRequestContext context = request.getRequestContext();
        return namingConvention.jcrPathForRequestPath(
                request.getPathInfo(),
                context.getSiteContentBaseBean().getPath());
    }

    protected static String xpath(String contentPath) {
        return String.format(
                "/jcr:root%s//element(*, govscot:basedocument)[hippostd:state = 'published'][hippostd:stateSummary = 'live']",
                contentPath);
    }

    private static Urlset getUrlSetForResults(NodeIterator it, HstRequest request) throws RepositoryException {
        Urlset urlset = new Urlset();
        HstRequestContext context = request.getRequestContext();
        Mount mount = context.getResolvedMount().getMount();
        HstLinkCreator linkCreator = context.getHstLinkCreator();
        while (it.hasNext()) {
            Node child = it.nextNode();
            String path = linkCreator.create(child, mount).toUrlForm(context, true);
            Url url = url(path, child);
            urlset.getUrls().add(url);
        }
        return urlset;
    }

    private static Url url(String path, Node node) throws RepositoryException {
        Url url = new Url();
        url.setLastmod(getLastModifiedDate(node));
        url.setLoc(path);
        return url;
    }

    private static Calendar getLastModifiedDate(Node node) throws RepositoryException {
        return node.getProperty("hippostdpubwf:lastModificationDate").getDate();
    }
}