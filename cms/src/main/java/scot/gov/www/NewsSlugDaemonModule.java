package scot.gov.www;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.onehippo.repository.events.HippoWorkflowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Allocate a slug to any new news then assign a slug to it.
 *
 * If the name already exists then disambiguate the slug by adding a number to the end.
 */
public class NewsSlugDaemonModule extends SlugDaemonModule {

    private static final Logger LOG = LoggerFactory.getLogger(NewsSlugDaemonModule.class);

    private static final String DOCUMENT_TYPE = "govscot:News";

    protected static final String PRGLOO_SLUG_PROPERTY = "govscot:prglooslug";

    private static final String PREFIX = "/content/documents/govscot/news/";

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

        if (!event.subjectPath().startsWith(PREFIX)) {
            return;
        }

        if (!event.success()) {
            return;
        }

        try {

            Node handle = session.getNode(event.returnValue()).getParent();

            if (handle == null) {
                LOG.info("handle was null: {}", event.subjectPath());
                return;
            }
            Node news = getLatestVariant(handle);
            assignSlug(news);
        } catch (RepositoryException ex) {
            LOG.error("Could not set publication slug for {}", ex, event.subjectPath());
        }
    }

    private void assignSlug(Node newsNode) throws RepositoryException {
        String name = newsNode.getName();
        if (newsNode.hasProperty(PRGLOO_SLUG_PROPERTY)) {
            name = newsNode.getProperty(PRGLOO_SLUG_PROPERTY).getString();
        }

        String slug = allocate(name, DOCUMENT_TYPE);
        LOG.info("assignSlug {} -> {}", newsNode.getPath(), slug);
        newsNode.setProperty(GOVSCOT_SLUG_PROPERTY, slug);
        session.save();
    }

}

