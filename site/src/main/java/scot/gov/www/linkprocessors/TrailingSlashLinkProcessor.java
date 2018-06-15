package scot.gov.www.linkprocessors;

import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.linking.HstLinkProcessorTemplate;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class TrailingSlashLinkProcessor extends HstLinkProcessorTemplate {

    @Override
    protected HstLink doPostProcess(HstLink link) {

        // if the path has an extension then leave it alone
        String extension = substringAfterLast(link.getPath(), ".");
        if (isNotEmpty(extension)) {
            return link;
        }

        // if the path ends in a slasg then leave it alone
        if (link.getPath().endsWith("/")) {

            return link;

        }

        // add a trailing slasg
        link.setPath(link.getPath() + "/");
        return link;
    }

    @Override
    protected HstLink doPreProcess(HstLink link) {
        return link;
    }

}