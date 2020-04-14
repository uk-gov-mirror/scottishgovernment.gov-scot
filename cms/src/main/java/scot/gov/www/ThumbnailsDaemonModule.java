package scot.gov.www;

import org.apache.commons.lang.time.StopWatch;
import scot.gov.www.exif.Exif;
import scot.gov.www.thumbnails.FileType;
import scot.gov.www.thumbnails.ThumbnailsProvider;
import scot.gov.www.thumbnails.ThumbnailsProviderException;
import org.apache.commons.io.FileUtils;
import org.hippoecm.repository.api.HippoNode;
import org.onehippo.repository.events.HippoWorkflowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Event listener to generate thumbnails whenever a document is edited.
 */
public class ThumbnailsDaemonModule extends DaemonModuleBase {

    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailsDaemonModule.class);

    ExecutorService executor = Executors.newFixedThreadPool(3);

    public boolean canHandleEvent(HippoWorkflowEvent event) {
        return event.success();
    }

    public void doHandleEvent(HippoWorkflowEvent event) throws RepositoryException {
        HippoNode subject = (HippoNode) session.getNodeByIdentifier(event.subjectId());
        if (!"govscot:document".equals(subject.getName())) {
            return;
        }

        executor.submit(() -> doRefreshThumbNails(event.subjectPath()));

    }

    // refresh the thumbnails for this document path, ensuring that we catch any repo exceptions
    void doRefreshThumbNails(String path) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            refreshThumbNails(path);
            LOG.info("refresh tyhumbnails for {} took {} millis", path, stopWatch.getTime());
        } catch (RepositoryException e) {
            LOG.error("Exception when calling session.refresh(false) while processing: " + path, e);
        }
    }

    void refreshThumbNails(String path) throws RepositoryException {
        try {
            Node node = session.getNode(path);
            deleteExistingThumbnails(node);
            createThumbnails(node);
            session.save();
        } catch (RepositoryException e) {
            session.refresh(false);
            LOG.error("Failed to generate thumbnail for: " + path, e);
        } catch (ThumbnailsProviderException | FileNotFoundException e) {
            LOG.error("Failed to generate thumbnail for: " + path, e);
        }
    }

    private void deleteExistingThumbnails(Node node) throws RepositoryException {
        NodeIterator nodeIterator = node.getParent().getNodes("govscot:thumbnails");
        while (nodeIterator.hasNext()) {
            Node thumbnail = nodeIterator.nextNode();
            thumbnail.remove();
        }
    }

    private void createThumbnails(Node documentNode)
            throws RepositoryException, FileNotFoundException, ThumbnailsProviderException {

        Node documentInformationNode = documentNode.getParent();
        Binary data = documentNode.getProperty("jcr:data").getBinary();
        String mimeType = documentNode.getProperty("jcr:mimeType").getString();
        String filename = documentNode.getProperty("hippo:filename").getString();

        if (mimeType == null) {
            LOG.warn("A document has been uploaded with no mimetype: {}", documentNode.getPath());
        }

        Map<Integer, File> thumbnails = ThumbnailsProvider.thumbnails(data.getStream(), mimeType);

        List<Integer> sortedKeys = new ArrayList<>(thumbnails.keySet());
        Collections.sort(sortedKeys);
        for (Integer size : sortedKeys) {
            File thumbnail = thumbnails.get(size);
            Node resourceNode = documentInformationNode.addNode("govscot:thumbnails", "hippo:resource");
            resourceNode.addMixin("hippo:skipindex");
            Binary binary = session.getValueFactory().createBinary(new FileInputStream(thumbnail));
            String thumbnailFilename = String.format("%s_%s.png", filename, size);
            resourceNode.setProperty("hippo:filename", thumbnailFilename);
            resourceNode.setProperty("jcr:data", binary);
            resourceNode.setProperty("jcr:mimeType", FileType.PNG.getMimeType());
            resourceNode.setProperty("jcr:lastModified", Calendar.getInstance());
            FileUtils.deleteQuietly(thumbnail);
        }

        documentInformationNode.setProperty("govscot:size", data.getSize());
        if (FileType.forMimeType(mimeType) == FileType.PDF) {
            documentInformationNode.setProperty("govscot:pageCount", Exif.pageCount(data));
        } else {
            documentInformationNode.setProperty("govscot:pageCount", 0);
        }
    }

}