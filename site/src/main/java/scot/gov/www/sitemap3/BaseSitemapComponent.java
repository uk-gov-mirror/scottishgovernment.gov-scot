package scot.gov.www.sitemap3;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.builder.Constraint;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;

import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.*;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.and;

/**
 * Common code used by sitemap components.
 */
public abstract class BaseSitemapComponent extends BaseHstComponent {

    static int MAX_SITEMAP_SIZE = 500;

    HstQuery allPagesQuery(HstRequest request) {
        return allPagesQuery(request, 0, 1);
    }

    HstQuery allPagesQuery(HstRequest request, int offset, int limit) {
        HstRequestContext context = request.getRequestContext();
        HippoBean baseBean = context.getSiteContentBaseBean();
        HstQueryBuilder builder = HstQueryBuilder.create(baseBean);
        String [] types = new String[] {
                "govscot:SimpleContent",
                "govscot:PublicationPage",
                "govscot:Collection",
                "govscot:ComplexDocumentSection"
        };

        HstQuery query = builder
                .ofTypes(types)
                .where(constraints())
                .limit(limit)
                .offset(offset)
                .orderByDescending("hippostdpubwf:lastModificationDate")
                .build();
        return query;
    }

    Constraint constraints() {
        return and(
                // exclude contents pages
                propertyFalseIfPresent("govscot:contentsPage"),

                // excelude those marked with exclude flag
                propertyFalseIfPresent("govscot:excludeFromSearchIndex")
        );
    }

    Constraint propertyFalseIfPresent(String property) {
        return or(constraint(property).notExists(), constraint(property).notEqualTo(true));
    }

    String createLink(HstRequest request, String path) {
        HstRequestContext context = request.getRequestContext();
        Mount mount = context.getResolvedMount().getMount();
        HstLinkCreator linkCreator = context.getHstLinkCreator();
        return linkCreator.create(path, mount).toUrlForm(context, true);
    }

}
