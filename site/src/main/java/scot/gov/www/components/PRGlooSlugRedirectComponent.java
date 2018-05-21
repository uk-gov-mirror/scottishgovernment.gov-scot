package scot.gov.www.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.request.ComponentConfiguration;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.util.HstResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.News;

import javax.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.constraint;

/**
 * redirect prgloo slugs
 */
public class PRGlooSlugRedirectComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DirectorateComponent.class);

    private static final String ARCHIVE_TEMPLATE = "https://www.webarchive.org.uk/wayback/archive/3000/http://news.gov.scot/%s";
    private Set<String> prglooSlugs;

    @Override
    public void init(ServletContext servletContext, ComponentConfiguration componentConfig) throws HstComponentException {
        super.init(servletContext, componentConfig);

        // parse the list of all know known prgloo slugs
        InputStream in = PRGlooSlugRedirectComponent.class.getResourceAsStream("/historicalNewsSlugs.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        prglooSlugs = reader.lines().collect(toSet());
        LOG.info("{} known prgloo slugs", prglooSlugs.size());
    }

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) {
        String slug = lastPathElement(request);
        HippoBean bean = findBySlug(request, slug);
        if (bean != null) {
            // this slug has been imported, redirect to it
            HstRequestContext context = request.getRequestContext();
            final HstLink link = context.getHstLinkCreator().create(bean, context);
            HstResponseUtils.sendPermanentRedirect(request, response, link.getPath());
            return;
        }

        if (prglooSlugs.contains(slug)) {
            // if it is a known prgloo slug then redirect to the archive
            String archiveUrl = String.format(ARCHIVE_TEMPLATE, slug);
            LOG.info("Redirecting slug {} to archive: {}", slug, archiveUrl);
            HstResponseUtils.sendPermanentRedirect(request, response, archiveUrl);
        }

        // this is not a slug we know how to handle, send a 404
        response.setStatus(404);
    }

    private HippoBean findBySlug(final HstRequest request, final String slug) {
        HstQuery query = HstQueryBuilder
                .create(request.getRequestContext().getSiteContentBaseBean())
                .ofTypes(News.class)
                .where(constraint("govscot:prglooslug").equalTo(slug))
                .build();
        return executeQuery(query, slug);
    }

    private String lastPathElement(HstRequest request) {
        String [] pathElements = request.getPathInfo().split("/");
        return pathElements[pathElements.length - 1];
    }

    private HippoBean executeQuery(HstQuery query, String slug) {
        try {
            HstQueryResult result = query.execute();
            if (result.getTotalSize() == 0) {
                LOG.warn("PRGloo slug not found: {}", slug);
                return null;
            }

            if (result.getTotalSize() > 1) {
                LOG.warn("Multiple news items with this slug : {}, will use first", slug);
            }

            return result.getHippoBeans().nextHippoBean();
        } catch (QueryException e) {
            LOG.error("Failed to get news by prgloo slug {}", slug, e);
            return null;
        }
    }
}
