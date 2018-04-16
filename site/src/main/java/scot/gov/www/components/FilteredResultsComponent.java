package scot.gov.www.components;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.builder.Constraint;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.hippoecm.hst.core.request.ComponentConfiguration;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.repository.util.DateTools;
import org.onehippo.cms7.essentials.components.EssentialsListComponent;
import org.onehippo.cms7.essentials.components.info.EssentialsListComponentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.News;
import scot.gov.www.beans.Policy;
import scot.gov.www.beans.Publication;

import javax.jcr.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.*;

/**
 * Created by z441571 on 09/04/2018.
 */
@ParametersInfo(type = EssentialsListComponentInfo.class)
public class FilteredResultsComponent extends EssentialsListComponent {

    private static final Logger LOG = LoggerFactory.getLogger(FilteredResultsComponent.class);

    private static Collection<String> FIELD_NAMES = new ArrayList<>();
    private static String PUBLISHED_DATE = "govscot:publishedDate";
    private static String PUBLICATION_DATE = "govscot:publicationDate";

    @Override
    public void init(ServletContext servletContext, ComponentConfiguration componentConfig) throws HstComponentException {
        super.init(servletContext, componentConfig);
        Collections.addAll(FIELD_NAMES, "govscot:title", "govscot:summary", "govscot:content", "hippostd:tags", 
                "govscot:incumbentTitle", "govscot:policyTags");
    }

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) {

        HippoBean bean = request.getRequestContext().getContentBean();
        try {
            HstQuery hstQuery;
            String path = bean.getNode().getPath();
            if (path.contains("news")) {
                hstQuery = HstQueryBuilder.create(bean)
                        .ofTypes(News.class)
                        .where(constraints(request, false))
                        .orderByDescending(PUBLISHED_DATE).build();
            } else if (path.contains("publications")) {
                hstQuery = HstQueryBuilder.create(bean)
                        .ofTypes(Publication.class)
                        .where(constraints(request, false))
                        .orderByDescending(PUBLICATION_DATE).build();
            } else {
                hstQuery = HstQueryBuilder.create(bean)
                        .ofTypes(Policy.class)
                        .where(constraints(request, true))
                        .orderByAscending("govscot:title").build();
            }
            HstQueryResult result = hstQuery.execute();
            request.setAttribute("result", result.getHippoBeans());
        } catch (RepositoryException e) {
            LOG.error("Failed to access repository", e);
        } catch (QueryException e) {
            LOG.error("Failed to execute query", e);
        }

        super.doBeforeRender(request, response);

    }

    private Constraint constraints(HstRequest request, boolean and) {

        List<Constraint> constraints = new ArrayList<>();
        addTermConstraints(constraints, request);
        addTopicsConstraint(constraints, request);
        addDateFilter(constraints, request);
        addPublicationTypeConstraint(constraints, request);
        if (and) {
            return and(constraints.toArray(new Constraint[constraints.size()]));
        }
        return or(constraints.toArray(new Constraint[constraints.size()]));
    }

    private void addTermConstraints(List<Constraint> constraints, HstRequest request) {
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

    private void addPublicationTypeConstraint(List<Constraint> constraints, HstRequest request) {
//        List<String> typesParam = Arrays.asList(param(request, "publicationType").split("|"));

        String typesParam = param(request, "publicationType");


        if (typesParam.isEmpty()) {
            return;
        }
        String [] publicationTypesArray = typesParam.split("\\|");

        for (String type : publicationTypesArray) {
            constraints.add(or(fieldConstraints(type)));
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
            List<String> begin = Arrays.asList(param(request, "begin").split("-"));
            if (begin.isEmpty()) {
                return;
            }

            Calendar calendar = new GregorianCalendar(
                    Integer.parseInt(begin.get(0)),
                    Integer.parseInt(begin.get(1))-1,
                    Integer.parseInt(begin.get(2)));
            constraints.add(and(constraint(PUBLICATION_DATE).greaterOrEqualThan(calendar, DateTools.Resolution.DAY)));
            constraints.add(and(constraint(PUBLISHED_DATE).greaterOrEqualThan(calendar, DateTools.Resolution.DAY)));

        }

        if (request.getParameterMap().containsKey("end")) {
            List<String> end = Arrays.asList(param(request, "end").split("-"));
            if (end.isEmpty()) {
                return;
            }

            Calendar calendar = new GregorianCalendar(
                    Integer.parseInt(end.get(0)),
                    Integer.parseInt(end.get(1))-1,
                    Integer.parseInt(end.get(2)));
            constraints.add(and(constraint(PUBLICATION_DATE).lessOrEqualThan(calendar, DateTools.Resolution.DAY)));
            constraints.add(and(constraint(PUBLISHED_DATE).lessOrEqualThan(calendar, DateTools.Resolution.DAY)));
        }
    }
}
