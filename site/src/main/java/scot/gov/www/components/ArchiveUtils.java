package scot.gov.www.components;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.util.HstResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class ArchiveUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveUtils.class);

    // Initially redirect to the current gov.scot site.
    // However we will need to decide wat happens when:
    // - www.gov.scot becomes www2.gov.scot
    // - when www2.gov.scot is decomissioned and publications are archived.
    private static final String ARCHIVE_TEMPLATE = "https://www.gov.scot%s";

    private static final boolean PERMANENT_ARCHIVE = false;

    public static void sendArchiveRedirect(String path, HstRequest request, HstResponse response) {
        String archiveUrl = String.format(ARCHIVE_TEMPLATE, path);
        LOG.info("Redirecting to archive {} -> {}", path, archiveUrl);
        if (PERMANENT_ARCHIVE) {
            HstResponseUtils.sendPermanentRedirect(request, response, archiveUrl);
        } else {
            HstResponseUtils.sendRedirect(request, response, archiveUrl);
        }
    }

    public static boolean isArchivedUrl(HstRequest request)  {
        try {
            Session session = request.getRequestContext().getSession();
            String path = String.format("/content/redirects/HistoricalUrls%s", request.getPathInfo());
            if (path.endsWith("/")) {
                path = StringUtils.substringBeforeLast(path, "/");
            }
            return session.nodeExists(path);
        } catch (RepositoryException e) {
            LOG.error("Failed to find publications redirect {}", request.getPathInfo(), e);
            return false;
        }
    }
}
