package scot.gov.www.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoFolderBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;

public class SiblingsComponent  extends BaseHstComponent {

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) throws HstComponentException {

        HstRequestContext ctx = request.getRequestContext();

        // get the content bean for the current resolved sitemap item and
        // set it on the request to make it available for
        // the renderer like jsp or freemarker
        HippoBean documentBean = ctx.getContentBean();

//        request.setAttribute("document", documentBean);
//
//        documentBean.getParentBean()
//        // get the content bean for the root of the current (sub)site and set
//        // it on the request to make it available for
//        // the renderer like jsp or freemarker
//        HippoBean  rootBean = ctx.getSiteContentBaseBean();
//        request.setAttribute("root",rootBean);
//
//        // get the base bean where all assets are stored, for example to use
//        // in a HstQuery
//        HippoFolderBean assetBaseBean = getAssetBaseBean(request);
//
//        assetBaseBean.
//        // get the base bean where all gallery items are stored, for example
//        // to use in a HstQuery
//        HippoFolderBean galleryBaseBean = getGalleryBaseBean(request);
    }

}
