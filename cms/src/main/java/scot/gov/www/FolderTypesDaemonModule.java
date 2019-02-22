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
import java.util.*;

/**
 * Maintain certain folders in sorted or
 */
public class FolderTypesDaemonModule implements DaemonModule {

    private static final Logger LOG = LoggerFactory.getLogger(FolderTypesDaemonModule.class);

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

        // we only want to listen to folder being added
        if (!"add".equals(event.action())) {
            return;
        }

        try {
            HippoNode newFolder = (HippoNode) session.getNode(event.result());
            if (!FolderUtils.hasFolderType(newFolder.getParent(), "new-publication-folder")) {
                return;
            }

            // sort the parent folder
            Node monthFolder = newFolder.getParent();
            sortChildren(monthFolder);
            session.save();
        } catch (RepositoryException e) {
            LOG.error("Unexpected exception while doing simple JCR read operations", e);
        }
    }

    // descide on sort order depending on the parent folder.....
    //    new publication      -> sort month ascending
    //    new month            -> sort year descending
    //    new year             -> sort type descending
    //    policy, group, topic -> sort partent ascending


    public void sortChildren(Node node) throws RepositoryException {
        List<String> sortedNames = sortedNames(node.getNodes());
        LOG.info("order: {}", sortedNames);
        for (int i = sortedNames.size() - 1; i >= 0; i--) {
            String before = sortedNames.get(i);
            String after = i < sortedNames.size() - 1 ? sortedNames.get(i + 1) : null;
            node.orderBefore(before, after);
        }
    }

    /**
     * Sort the nodes in an iterator, Folders in alphabetical order first then other documents in alphabetical order.
     */
    List<String> sortedNames(NodeIterator it) throws RepositoryException {

        // for each node work out what name we want to sort by and
        // partition them into folders and 'others'
        Map<String, String> nameMap = new HashMap<>();
        List<String> folders = new ArrayList<>();
        List<String> others = new ArrayList<>();
        apply(it, node -> {
            nameMap.put(node.getName(), name(node));
            if (isHippoFolder(node)) {
                folders.add(node.getName());
            } else {
                others.add(node.getName());
            }
        });

        folders.sort(compareNodeNames(nameMap));
        others.sort(compareNodeNames(nameMap));

        List<String> names = new ArrayList<>();
        names.addAll(folders);
        names.addAll(others);
        return names;
    }

    String name(Node node) throws RepositoryException {
        if (node.hasProperty("hippo:name")) {
            return node.getProperty("hippo:name").getString();
        } else {
            return node.getName();
        }
    }

    Comparator<String> compareNodeNames(Map<String, String> nameMap) {
        return (l, r) -> String.CASE_INSENSITIVE_ORDER.compare(nameMap.get(l), nameMap.get(r));
    }

    void apply(NodeIterator it, ThrowingConsumer consumer) throws RepositoryException {
        while (it.hasNext()) {
            consumer.accept(it.nextNode());
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer {
        void accept(Node t) throws RepositoryException;
    }

    boolean isHippoFolder(Node node) throws RepositoryException {
        return "hippostd:folder".equals(node.getPrimaryNodeType().getName());
    }

}