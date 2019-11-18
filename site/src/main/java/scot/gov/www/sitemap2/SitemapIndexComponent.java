package scot.gov.www.sitemap2;

import org.apache.commons.lang.time.StopWatch;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.forge.sitemap.components.model.sitemapindex.SitemapIndex;
import org.onehippo.forge.sitemap.components.model.sitemapindex.TSitemap;
import org.onehippo.forge.sitemap.generator.SitemapIndexGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Component that generates a sitemap index file for a site.
 *
 * // TODO: special case for root sitemap...
 */
public class SitemapIndexComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SitemapIndexComponent.class);

    private static NamingConvention namingConvention = new NamingConvention();

    private static SplitPolicy splitPolicy = new SplitPolicy();

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) {
        super.doBeforeRender(request, response);

        LOG.info("Generating sitemap index for {}", request.getPathInfo());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            SitemapIndex sitemapIndex = generateSitemap(request);
            request.setAttribute("sitemap", SitemapIndexGenerator.toString(sitemapIndex));
            LOG.info("Finished generating sitemap index {} pages, took {} millis",
                    request.getPathInfo(), sitemapIndex.getSitemap().size(), stopWatch.getTime());
        } catch (RepositoryException e) {
            LOG.error("Failed top generate sitemap index", e);
            throw new HstComponentException(e);
        }
    }

    SitemapIndex generateSitemap(HstRequest request) throws RepositoryException {
        SitemapIndex sitemapIndex = new SitemapIndex();
        Node root = request.getRequestContext().getSiteContentBaseBean().getNode();
        sitemapIndex.getSitemap().addAll(getSitemaps(root, request));
        return sitemapIndex;
    }

    List<TSitemap> getSitemaps(Node node, HstRequest request) throws RepositoryException {
        List<TSitemap> sitemaps = new ArrayList<>();
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node child = it.nextNode();
            if (child.isNodeType("hippostd:folder")) {
                sitemaps.addAll(sitemapsForNode(child, request));
            }
        }
        return sitemaps;
    }

    List<TSitemap> sitemapsForNode(Node node, HstRequest request) throws RepositoryException {
        return splitPolicy.shouldSplit(node)
                ? getSitemaps(node, request)
                : singletonList(sitemap(node, request));
    }

    TSitemap sitemap(Node node, HstRequest request) throws RepositoryException {
        TSitemap sitemap = new TSitemap();
        HstRequestContext context = request.getRequestContext();
        Mount mount = context.getResolvedMount().getMount();
        HstLinkCreator linkCreator = context.getHstLinkCreator();
        String requestPath = context.getSiteContentBaseBean().getPath();
        String path = namingConvention.requestPathForJcrPath(node.getPath(), requestPath);
        String nodeUrl = linkCreator.create(path, mount).toUrlForm(context, true);
        sitemap.setLoc(nodeUrl);
        return sitemap;
    }

}
