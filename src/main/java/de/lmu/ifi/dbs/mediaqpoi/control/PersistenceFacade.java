package de.lmu.ifi.dbs.mediaqpoi.control;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.search.*;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import javax.jdo.*;
import javax.jdo.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PersistenceFacade {

    private static final Logger LOGGER = Logger.getLogger(PersistenceFacade.class.getName());
    private static final String INDEX_VIDEOS = "index_videos";

    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");

    private PersistenceFacade() {
    }

    private static PersistenceManager getPersistenceManager() {
        return pmfInstance.getPersistenceManager();
    }

    public static Video getVideo(Key key) throws Exception {
        try {
            PersistenceManager pm = getPersistenceManager();
            return pm.getObjectById(Video.class, key);
        } catch (Exception e) {
            LOGGER.severe("Exception while getting video by key: " + e);
            throw e;
        }
    }

    public static List<Video> getVideos() throws Exception {
        PersistenceManager pm = getPersistenceManager();
        Query q = pm.newQuery(Video.class);
        try {
            pm = getPersistenceManager();
            q = pm.newQuery(Video.class);
            return (List<Video>) q.execute();
        } catch (Exception e) {
            LOGGER.severe("Exception while getting all videos: " + e);
            throw e;
        } finally {
            q.closeAll();
        }
    }

    public static void persistVideo(Video video) throws Exception {
        PersistenceManager pm = getPersistenceManager();

        // persist video and dependent entities in a transaction
        Transaction tx = null;
        try {
            tx = pm.currentTransaction();
            tx.begin();
            pm.makePersistent(video);
            tx.commit();
        } catch (Exception e) {
            LOGGER.severe("Exception while persisting video: " + e);
            throw e;
        } finally {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        }
    }

    public static void indexVideos(List<Video> videos) throws Exception {
        try {
            List<Document> documents = new ArrayList<>();
            for (Video video : videos) {
                if (video.getTrajectory() != null) {
                    documents.add(createDocument(video));
                }
            }

            indexDocuments(documents);
        } catch (Exception e) {
            LOGGER.severe("Exception while indexing videos: " + e);
            e.printStackTrace();
            throw e;
        }
    }

    private static Document createDocument(Video video) throws Exception {
        try {
            String docId = video.getFileName();
            Trajectory trajectory = video.getTrajectory();
            GeoPoint maxPoint = trajectory.getMaxLocation().toGeoPoint();
            GeoPoint minPoint = trajectory.getMinLocation().toGeoPoint();

            return Document.newBuilder().setId(docId)
                .addField(Field.newBuilder().setName("maxPoint").setGeoPoint(maxPoint))
                .addField(Field.newBuilder().setName("minPoint").setGeoPoint(minPoint))
                .build();
        } catch (Exception e) {
            LOGGER.severe("Exception while creating document for video: " + e);
            throw e;
        }
    }

    private static void indexDocuments(List<Document> documents) throws Exception {
        try {
            IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_VIDEOS).build();
            Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

            List<List<Document>> documentBatches = chunks(documents, 200);
            LOGGER.info(documentBatches.size() + " batch(es) to process");

            // "You can pass up to 200 documents at a time to the put() method.
            // Batching puts is more efficient than adding documents one at a time." (Google Doc)
            for (List<Document> batch : documentBatches) {
                LOGGER.info(batch.size() + " document(s) to be processed in this batch  ");
                index.put(batch);
            }

        } catch (Exception e) {
            LOGGER.severe("Exception while indexing documents: " + e);
            throw e;
        }
    }

    private static <T> List<List<T>> chunks(List<T> bigList, int n) {
        List<List<T>> chunks = new ArrayList<>();

        for (int i = 0; i < bigList.size(); i += n) {
            List<T> chunk = bigList.subList(i, Math.min(bigList.size(), i + n));
            chunks.add(chunk);
        }

        return chunks;
    }
}
