package scot.gov.www;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Event listener to set the publication type field depending on the folder.
 */
public class PublicationTypeDaemonModule implements DaemonModule {

    private static final Logger LOG = LoggerFactory.getLogger(FolderTypesDaemonModule.class);

    private static final Set<String> PUBLICATION_TYPES = unmodifiableSet(new HashSet<>(asList(
            "govscot:Publication",
            "govscot:ComplexDocument",
            "govscot:Minutes",
            "govscot:SpeechOrStatement"
    )));

    private static final String PUBLICATIONS_PREFIX = "/content/documents/govscot/publications";

    public static final String PUBLICATION_TYPE = "govscot:publicationType";

    private Session session;

    @Override
    public void initialize(Session session) throws RepositoryException {
        this.session = session;
        HippoServiceRegistry.registerService(this, HippoEventBus.class);
    }

    @Override
    public void shutdown() {
        HippoServiceRegistry.unregisterService(this, HippoEventBus.class);
    }

    @Subscribe
    public void handleEvent(HippoWorkflowEvent event) {
        if (!event.success()) {
            return;
        }

        if (!event.subjectPath().startsWith(PUBLICATIONS_PREFIX)) {
            return;
        }

        try {
            Node handle = null;

            if (isNewPublicationDocument(event)) {
                handle = session.getNode(event.returnValue()).getNode("index");
            } else if (isPublicationEdit(event)) {
                handle = session.getNodeByIdentifier(event.subjectId());
            }

            if (handle == null) {
                return;
            }

            HippoNode publication = (HippoNode) getLatestVariant(handle);
            String publicationType = findPublicationType(publication);

            setPublicationType(publication, publicationType);
        } catch (RepositoryException e) {
            LOG.error("Unexpected exception while doing simple JCR read operations", e);
        }
    }

    private boolean isNewPublicationDocument(HippoWorkflowEvent event) {
        return event.arguments().contains("hippostd:folder")
                && "threepane:folder-permissions:add".equals(event.interaction());
    }

    private boolean isPublicationEdit(HippoWorkflowEvent event) {
        return PUBLICATION_TYPES.contains(event.documentType());
    }

    private String findPublicationType(Node node) throws RepositoryException {
        Path path = Paths.get(node.getPath());
        int depth = Paths.get(PUBLICATIONS_PREFIX).getNameCount();
        return path.subpath(depth, depth + 1).toString();
    }

    private static Node getLatestVariant(Node handle) throws RepositoryException {
        NodeIterator it = handle.getNodes();
        Node variant = null;
        while (it.hasNext()) {
            variant = it.nextNode();
        }
        return variant;
    }

    private void setPublicationType(HippoNode publication, String publicationType) throws RepositoryException {
        if (!publication.hasProperty(PUBLICATION_TYPE)
                || !publication.getProperty(PUBLICATION_TYPE).equals(publicationType)) {
            publication.setProperty(PUBLICATION_TYPE, publicationType);
            session.save();
        }
    }

}
