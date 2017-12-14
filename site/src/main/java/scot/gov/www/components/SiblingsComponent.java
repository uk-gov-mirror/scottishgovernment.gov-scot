package scot.gov.www.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;

import scot.gov.www.beans.Policy;
import scot.gov.www.beans.SimpleDocument;
import scot.gov.www.beans.Topic;

import java.util.ArrayList;
import java.util.List;

public class SiblingsComponent  extends BaseHstComponent {

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) throws HstComponentException {

        HstRequestContext ctx = request.getRequestContext();
        HippoBean documentBean = ctx.getContentBean();
        request.setAttribute("document", documentBean);
        HippoBean parent = documentBean.getParentBean();
        request.setAttribute("policypages", parent.getChildBeans(SimpleDocument.class));

        List<Policy> policies = parent.getChildBeans(Policy.class);
        if (policies.size() == 1) {
            request.setAttribute("overview", policies.get(0));
        }
    }
}
