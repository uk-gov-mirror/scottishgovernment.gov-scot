package scot.gov.www.components;

import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.request.HstRequestContext;
import scot.gov.www.beans.Policy;
import scot.gov.www.beans.Topic;

import java.util.ArrayList;
import java.util.List;

public class TopicsProvider {

    /**
     * Provider topics and issues for this bean.
     *
     * @param ctx
     * @param request
     * @throws HstComponentException
     */
    public void provideTopics(HstRequestContext ctx, HstRequest request)  throws HstComponentException{
        HippoBean scope = ctx.getSiteContentBaseBean();

        try {
            HstQuery hstQuery = ctx.getQueryManager().createQuery(scope, false, Topic.class, Policy.class);
            Filter filter = hstQuery.createFilter();

            filter.addEqualTo("govscot:title", "Economy");
            hstQuery.setFilter(filter);
            HstQueryResult result = hstQuery.execute();

            HippoBeanIterator beanIterator = result.getHippoBeans();
            List<HippoBean> topics = new ArrayList<>();
            while (beanIterator.hasNext()) {
                topics.add((Topic) beanIterator.next());
            }
            request.setAttribute("topics", topics);
        } catch (QueryException e) {
            throw new HstComponentException("Exception occured during creation or execution of HstQuery.", e);
        }
    }
}
