package scot.gov.www.components;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.builder.Constraint;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.ComponentConfiguration;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.News;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.constraint;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.or;

public class NewsLandingPageComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(NewsLandingPageComponent.class);

    private static Collection<String> FIELD_NAMES = new ArrayList<>();

    private Map<String, String> topicMap = new HashMap<>();

    @Override
    public void init(ServletContext servletContext, ComponentConfiguration componentConfig) throws HstComponentException {
        super.init(servletContext, componentConfig);
        Collections.addAll(FIELD_NAMES, "govscot:title", "govscot:summary", "govscot:content", "hippostd:tags");
    }

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) {
        HippoBean scope = request.getRequestContext().getSiteContentBaseBean();
        HstQueryBuilder queryBuilder = HstQueryBuilder.create(scope)
                .ofTypes(News.class)
                .where(constraints(request))
                .orderByDescending("govscot:publishedDate");
        try {
            HstQuery query = queryBuilder.build();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HstQueryResult results = query.execute();
            stopWatch.stop();
            LOG.info("result count: {}, took: {}", results.getTotalSize(), stopWatch.getTime());
            request.setAttribute("results", results);
        } catch (QueryException e) {
            throw new HstComponentException(e);
        }
    }

    private Constraint constraints(HstRequest request) {

        List<Constraint> constraints = new ArrayList<>();
        addTermConstrainst(constraints, request);
        addTopicsConstraint(constraints, request);
        addDateFilter(constraints, request);
        return or(constraints.toArray(new Constraint[constraints.size()]));
    }

    private void addTermConstrainst(List<Constraint> constraints, HstRequest request) {
        String term = param(request, "term");
        if (StringUtils.isBlank(term)) {
            return;
        }
        constraints.add(or(fieldConstraints(term)));
    }

    private Constraint [] fieldConstraints(String term) {

        List<Constraint> constraints = FIELD_NAMES
                .stream()
                .map(field -> constraint(field).contains(term))
                .collect(toList());
        return constraints.toArray(new Constraint[constraints.size()]);
    }

    private void addTopicsConstraint(List<Constraint> constraints, HstRequest request) {
        List<String> topicIds = topicIds(request);
        if (topicIds.isEmpty()) {
            return;
        }

        for (String topicId : topicIds) {
            // is this the right constraint?
            System.out.println("HERE " + topicId);
            constraints.add(constraint("govscot:topics/@hippo:docbase").equalTo(topicId));
        }
    }

    private List<String> topicIds(HstRequest request) {
        List<String> topicIds = new ArrayList<>();
        Set<String> topics = topics(request);
        try {
            Session session = request.getRequestContext().getSession();
            Node topicsNode = session.getNode("/content/documents/govscot/topics");
            if (topicsNode == null) {
                return Collections.emptyList();
            }

            NodeIterator nodeIt = topicsNode.getNodes();
            while (nodeIt.hasNext()) {
                Node topicNode = nodeIt.nextNode();

                if (isRequiredTopic(topicNode, topics)) {
                    topicIds.add(topicNode.getIdentifier());
                }
            }
            return topicIds;
        } catch (RepositoryException e) {
            throw new HstComponentException("Failed to get topics", e);
        }
    }

    private String param(HstRequest request, String param) {
        HstRequestContext requestContext = request.getRequestContext();
        HttpServletRequest servletRequest = requestContext.getServletRequest();
        return servletRequest.getParameter(param);
    }

    private Set<String> topics(HstRequest request) {
        String topicsParam = param(request, "topics");
        if (topicsParam == null) {
            return Collections.emptySet();
        }
        String [] topicTitleArray = topicsParam.split("\\|");
        return new HashSet<>(Arrays.asList(topicTitleArray));
    }

    private boolean isRequiredTopic(Node topicNode, Set<String> requiredTopicTitles) throws RepositoryException {
        String title = nodeTitle(topicNode);
        return requiredTopicTitles.contains(title);
    }

    private String nodeTitle(Node node) throws RepositoryException {
        Property titleProperty = node.getProperty("hippo:name");
        return titleProperty.getString();
    }

    private void addDateFilter(List<Constraint> constraints, HstRequest request) {
        if (request.getParameterMap().containsKey("begin")) {
        }

        if (request.getParameterMap().containsKey("end")) {
        }
    }

}
