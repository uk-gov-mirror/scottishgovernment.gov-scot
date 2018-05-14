package scot.gov.www.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.onehippo.taxonomy.api.Taxonomy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.Issue;
import scot.gov.www.beans.Topic;
import scot.gov.www.components.info.FilteredResultsSideComponentInfo;
import scot.gov.www.components.mapper.TaxonomyMapper;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by z441571 on 09/04/2018.
 */
@ParametersInfo(type = FilteredResultsSideComponentInfo.class)
public class FilteredResultsSideComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(FilteredResultsSideComponent.class);
    private static final String TOPICS = "topics";

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) {
        super.doBeforeRender(request, response);
        FilteredResultsSideComponentInfo info = getComponentParametersInfo(request);

        HippoBean baseBean = request.getRequestContext().getSiteContentBaseBean();

        HstQuery query = HstQueryBuilder.create(baseBean)
                .ofTypes(Issue.class, Topic.class).orderByAscending("govscot:title").build();

        TaxonomyMapper mapper = TaxonomyMapper.getInstance();
        Taxonomy publicationTypes = mapper.getPublicationTypesTaxonomy();

        request.setAttribute("term", true);
        if (info.getIncludeDateFilter()) {
            request.setAttribute("dates", true);
            request.setAttribute("fromDate", info.getFromDate());
        }
        if (info.getIncludePublicationTypesFilter()) {
            request.setAttribute("publicationTypes", publicationTypes);
        }
        if (info.getLocaleRequired()) {
            request.setAttribute("locale", request.getLocale());
        }
        executeQueryLoggingException(query, request, TOPICS);
        request.setAttribute("searchType", info.getSearchType());

        Map<String, Set<String>> params = sanitiseParameterMap(request,
            request.getRequestContext().getServletRequest().getParameterMap());
        request.setAttribute("parameters", params);

    }

    private void executeQueryLoggingException(HstQuery query, HstRequest request, String name) {
        try {
            HstQueryResult result = query.execute();
            request.setAttribute(name, result.getHippoBeans());
        } catch (QueryException e) {
            LOG.error("Failed to get {}", name, e);
        }
    }

    private Map<String, Set<String>> sanitiseParameterMap(HstRequest request, Map<String, String[]> parameterMap) {
        if (parameterMap == null) {
            return null;
        }
        Map<String, Set<String>> sanitisedMap = new HashMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            sanitisedMap.put(entry.getKey(), splitParameters(request, entry.getKey()));
        }
        return sanitisedMap;
    }

    private Set<String> splitParameters(HstRequest request, String parameter) {
        String parameters = param(request, parameter);
        if (parameters == null) {
            return Collections.emptySet();
        }
        String [] topicTitleArray = parameters.split("\\;");
        return new HashSet<>(Arrays.asList(topicTitleArray));
    }

    private String param(HstRequest request, String param) {
        HstRequestContext requestContext = request.getRequestContext();
        HttpServletRequest servletRequest = requestContext.getServletRequest();
        return servletRequest.getParameter(param);
    }
}
