package scot.gov.www;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.linking.HstLinkProcessorTemplate;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.Arrays;

public class NewsLinkProcessor extends HstLinkProcessorTemplate {
    @Override
    protected HstLink doPostProcess(HstLink link) {
        if (isNewsLink(link)) {
            // remove the date and time...
            // Annoyingly they normalise this path to remove any trailing slash
            link.setPath(String.format("news/%s", link.getPathElements()[3]));
        }

        return link;
    }

    @Override
    protected HstLink doPreProcess(HstLink link) {
        if (isNewsLink(link)) {
            return preProcessNewsLink(link);
        }
        return link;
    }

    boolean isNewsLink(HstLink link) {
        return link.getPath().startsWith("news/");
    }

    HstLink preProcessNewsLink(HstLink link) {
        HstRequestContext req = RequestContextProvider.get();

        try {
            String slug = link.getPathElements()[link.getPathElements().length - 1];
            Node handle = getHandleBySlug(slug);
            String newPath = String.format("news/%s", StringUtils.substringAfter(handle.getPath(), "news/"));
            link.setPath(newPath);
            return link;
        } catch (RepositoryException e) {
            throw new RuntimeException("arhg", e);
        }
    }

    Node getHandleBySlug(String slug) throws RepositoryException {
        HstRequestContext req = RequestContextProvider.get();
        Session session = req.getSession();
        String sql = String.format("SELECT * FROM govscot:News WHERE jcr:path LIKE '%%/%s'", slug);
        QueryResult result = session.getWorkspace().getQueryManager().createQuery(sql, Query.SQL).execute();
        if (result.getNodes().getSize() == 0) {
            return null;
        }
        return result.getNodes().nextNode().getParent();
    }
}