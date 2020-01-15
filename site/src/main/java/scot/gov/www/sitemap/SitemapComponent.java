package scot.gov.www.sitemap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;

import javax.jcr.RepositoryException;

import org.onehippo.forge.sitemap.components.model.Url;
import org.onehippo.forge.sitemap.components.model.Urlset;
import org.onehippo.forge.sitemap.generator.SitemapGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import static org.apache.commons.lang.StringUtils.substringAfter;

/**
 * Component that produces a sitemap for a part of the content tree.  Uses code from the bloomreach forge plugin to
 * produce the XML.
 */
public class SitemapComponent extends BaseSitemapComponent {

    private static final Logger LOG = LoggerFactory.getLogger(SitemapComponent.class);

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
        } catch (QueryException | RepositoryException e) {
            LOG.error("Failed to generate sitemap", e);
            throw new HstComponentException(e);
        }
    }

    private Urlset generateSitemap(HstRequest request) throws QueryException, RepositoryException {
        HippoBeanIterator it = getPublishedNodesForRequest(request);
        return getUrlSetForResults(it, request);
    }

    private HippoBeanIterator getPublishedNodesForRequest(HstRequest request) throws QueryException {
        int offset = getOffsetFromRequestPath(request.getPathInfo());
        HstQuery query = allPagesQuery(request, offset, MAX_SITEMAP_SIZE);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        HippoBeanIterator it = query.execute().getHippoBeans();
        LOG.info("query took {} millis", stopWatch.getTime());
        return it;
    }>

    private int getOffsetFromRequestPath(String path) {
        String stripped = StringUtils.substringBefore(substringAfter(path, "/sitemap_"), ".xml");
        try {
            int index = Integer.valueOf(stripped);
            return index * MAX_SITEMAP_SIZE;
        } catch (NumberFormatException e) {
            throw new HstComponentException(String.format("Invalid sitemap index in request path: %s", path));
        }
    }

    private static Urlset getUrlSetForResults(HippoBeanIterator it, HstRequest request) throws RepositoryException {
        Urlset urlset = new Urlset();
        HstRequestContext context = request.getRequestContext();
        HstLinkCreator linkCreator = context.getHstLinkCreator();
        while (it.hasNext()) {
            HippoBean child = it.nextHippoBean();
            //String path = linkCreator.create(child, request.getRequestContext()).toUrlForm(context, true);
            String path = child.getName();
            Url url = url(path, child);
            urlset.getUrls().add(url);
        }
        return urlset;
    }

    // almost everything is publications or news, can we just hack the creation of these urls if it is faster?
    private static Url url(String path, HippoBean bean) throws RepositoryException {
        Url url = new Url();
        url.setLastmod(getLastModifiedDate(bean));
        url.setLoc(path);
        return url;
    }

    private static Calendar getLastModifiedDate(HippoBean bean) throws RepositoryException {
        return bean.getProperty("hippostdpubwf:lastModificationDate");
    }
}