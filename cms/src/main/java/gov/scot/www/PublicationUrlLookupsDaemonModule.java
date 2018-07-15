package gov.scot.www;

import org.hippoecm.repository.api.HippoNode;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.onehippo.repository.events.HippoWorkflowEvent;
import org.onehippo.repository.modules.DaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Event listener to generate url lookups for publications.
 */
public class PublicationUrlLookupsDaemonModule implements DaemonModule {

    private static final Logger LOG = LoggerFactory.getLogger(PublicationUrlLookupsDaemonModule.class);

    private static final String URL_LOOKUP_ROOT = "/content/urllookups/publications/";

    private Session session;

    @Override
    public void initialize(final Session session) throws RepositoryException {
        this.session = session;
        HippoServiceRegistry.registerService(this, HippoEventBus.class);
    }

    @Override
    public void shutdown() {
        HippoServiceRegistry.unregisterService(this, HippoEventBus.class);
    }

    @Subscribe
    public void handleEvent(final HippoWorkflowEvent event) {
        if (!event.success()) {
            return;
        }

        try {
            HippoNode subject = (HippoNode) session.getNodeByIdentifier(event.subjectId());
            if (isPublicationHandle(subject)) {
                createPublicationUrlLookup(subject);
            }
        } catch (RepositoryException e) {
            LOG.error("Unexpected exception while doing simple JCR read operations", e);
        }
    }

    private boolean isPublicationHandle(HippoNode node) throws RepositoryException {
        NodeIterator nodeIt = node.getNodes(node.getName());
        if (nodeIt.hasNext()) {
            return nodeIt.nextNode().isNodeType("govscot:Publication");
        }
        return false;
    }

    private void createPublicationUrlLookup(HippoNode publication) throws RepositoryException {
        Node root = session.getNode(URL_LOOKUP_ROOT);
        Node lookupNode = ensureUnstructuredPathInternal(root, 0, lettersAsList(publication.getName()));
        lookupNode.setProperty("path", publication.getPath());
        session.save();
    }

    private List<String> lettersAsList(String str) {
        return str.chars().mapToObj(e->(char) e).map(ch -> Character.toString(ch)).collect(toList());
    }

    private Node ensureUnstructuredPathInternal(Node parent, int pos, List<String> path) throws RepositoryException {

        if (pos == path.size()) {
            return parent;
        }

        String element = path.get(pos);
        Node next = parent.hasNode(element)
                ? parent.getNode(element)
                : unstructuredNode(parent, element);
        int newPos = pos + 1;
        return ensureUnstructuredPathInternal(next, newPos, path);
    }

    private Node unstructuredNode(Node parent, String name) throws RepositoryException {
        return parent.addNode(name, "nt:unstructured");
    }
}