package de.lmu.ifi.dbs.mediaqpoi.control;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.*;
import com.google.apphosting.api.ApiProxy;
import de.lmu.ifi.dbs.mediaqpoi.entity.Location;
import de.lmu.ifi.dbs.mediaqpoi.entity.Trajectory;
import de.lmu.ifi.dbs.mediaqpoi.entity.TrajectoryPoint;
import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public final class PersistenceFacade {

    private static final Logger LOGGER = Logger.getLogger(PersistenceFacade.class.getName());
    private static final String INDEX_VIDEOS = "index_videos";

    private static final PersistenceManagerFactory pmfInstance =
        JDOHelper.getPersistenceManagerFactory("transactions-optional");
    public static final int RESULT_LIMIT = 1000;

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
        javax.jdo.Query q = pm.newQuery(Video.class);
        try {
            pm = getPersistenceManager();
            q = pm.newQuery(Video.class);
            List<Video> allVideos = (List<Video>) q.execute();
            LOGGER.info(String.format("Found %s videos", allVideos.size()));
            return allVideos;
        } catch (Exception e) {
            LOGGER.severe("Exception while getting all videos: " + e);
            throw e;
        } finally {
            q.closeAll();
        }
    }

    public static List<Video> getVideosInRange(Location northEast, Location southWest) throws Exception {
        try {
            Location middle = GeoHelper.getMidPoint(northEast, southWest);
            String distanceToMiddle = Double.toString(GeoHelper.getDistanceInMeters(northEast, middle));
            // get videos with a min or max point in the circumcircle around the range (they are potential candidates)
            String queryString = "distance(minPoint, " + geoPoint(middle) + ") <= " + distanceToMiddle;
            queryString += " OR distance(maxPoint, " + geoPoint(middle) + ") <= " + distanceToMiddle;
            Query query =   Query.newBuilder()
                .setOptions(QueryOptions.newBuilder()
                                .setNumberFoundAccuracy(RESULT_LIMIT)
                                .setReturningIdsOnly(true)
                                .setLimit(RESULT_LIMIT).build())
                .build(queryString);
            Results<ScoredDocument> results = getIndex().search(query);

            LOGGER.info(results.getNumberFound() + " candidate results found");

            List<Video> videos = new CopyOnWriteArrayList<>();
            // refinement: walk through candidates and look if the video is really in range
            for (ScoredDocument document : results) {
                Video video = getVideo(document);
                if (video == null) {
                    continue;
                }

                Trajectory trajectory = video.getTrajectory();
                if (GeoHelper.isInRange(trajectory, northEast, southWest)) {
                    videos.add(video);
                }
            }

            LOGGER.info(String.format("Found %s videos for the given range (using Google document index)", videos.size()));
            return videos;

        } catch(Exception e) {
            LOGGER.severe("Exception while getting videos in range: " + e);
            throw e;
        }
    }

    public static List<Video> getVideos(double longitude, double latitude) throws Exception {
        try {
            Location location = new Location(latitude, longitude);

            long maxSearchRange = getMaxSearchRange();

            // get candidates by upper bound which is the maximum search range. Every center point of a trajectory that is even more far away than this upper bound cannot be a result
            String queryString = "distance(centerPoint, " + geoPoint(location) + ") <= " + maxSearchRange;
            Query query = Query.newBuilder()
                    .setOptions(QueryOptions.newBuilder()
                            .setNumberFoundAccuracy(RESULT_LIMIT)
                            .setReturningIdsOnly(true)
                            .setLimit(RESULT_LIMIT).build())
                    .build(queryString);

            Results<ScoredDocument> candidates = getIndex().search(query);
            LOGGER.info(candidates.getNumberFound() + " candidates found.");

            List<Video> videos = new CopyOnWriteArrayList<>();
            // refinement: walk through candidates and look if the location really is visible in the video
            for (ScoredDocument document : candidates) {
                Video video = getVideo(document);

                if (video == null) {
                    continue;
                }

                Trajectory trajectory = video.getTrajectory();
                if (trajectory == null || trajectory.getTimeStampedPoints() == null) {
                    LOGGER.warning("Video " + video.getFileName() + " has no trajectory data");
                    continue;
                }

                for (TrajectoryPoint point : trajectory.getTimeStampedPoints()) {
                    if (point.isVisible(latitude, longitude)) {
                        videos.add(video);
                        break;
                    }
                }
            }

            LOGGER.info(String.format("Found %s videos for the given location (using Google document index)", videos.size()));
            return videos;
        } catch (Exception e) {
            LOGGER.severe("Exception while getting videos for location: " + e);
            throw e;
        }
    }





    /**
    * Do not use this method. It doesn't work because the query is not supported by Google Search API (see my question on stackoverflow
     * http://stackoverflow.com/questions/28213499/is-it-possible-to-use-a-document-field-as-radius-with-distance-function-in-googl)
     */
    @Deprecated
    public static List<Video> getVideosOld(double longitude, double latitude) throws Exception {
        try {
            Location location = new Location(latitude, longitude);
            // get videos that contain the location in their search range (they are potential candidates)
            String queryString = "distance(centerPoint, " + geoPoint(location) + ") <= searchRange";
            Query query =   Query.newBuilder()
                .setOptions(QueryOptions.newBuilder()
                                .setNumberFoundAccuracy(RESULT_LIMIT)
                                .setReturningIdsOnly(true)
                                .setLimit(RESULT_LIMIT).build())
                .build(queryString);

            Results<ScoredDocument> results = getIndex().search(query);

            LOGGER.info(results.getNumberFound() + " candidate results found");

            List<Video> videos = new CopyOnWriteArrayList<>();
            // refinement: walk through candidates and look if the location really is visible in the video
            for (ScoredDocument document : results) {
                Video video = getVideo(document);
                if (video == null) {
                    continue;
                }

                Trajectory trajectory = video.getTrajectory();
                if(trajectory == null || trajectory.getTimeStampedPoints() == null) {
                    LOGGER.warning("Video " + video.getFileName() + " has no trajectory data");
                    continue;
                }
                for (TrajectoryPoint point : trajectory.getTimeStampedPoints()) {
                    if (point.isVisible(longitude, latitude)) {
                        videos.add(video);
                        break;
                    }
                }
            }

            LOGGER.info(String.format("Found %s videos for the given location (using Google document index)", videos.size()));
            return videos;

        } catch(Exception e) {
            LOGGER.severe("Exception while getting videos for location: " + e);
            throw e;
        }
    }

    public static void persistVideos(List<Video> videos) throws Exception {
        LOGGER.info("Persisting " + videos.size() + " videos");
        PersistenceManager pm = getPersistenceManager();

        cleanExistingVideos(pm, videos);

        List<List<Video>> batches = chunks(videos, 5);
        LOGGER.info(batches.size() + " batch(es) to process");

        long persistedVideos = 0;
        // XG transactions allow a maximum of five EGs per transaction. The 6th produces "java.lang.IllegalArgumentException: operating on too many entity groups in a single transaction."
        for (List<Video> batch : batches) {
            LOGGER.info(batch.size() + " video(s) to be processed in this batch.");

            // persist videos and dependent entities in a transaction
            Transaction tx = null;
            try {
                tx = pm.currentTransaction();
                tx.begin();
                pm.makePersistentAll(batch);
                tx.commit();
                persistedVideos += batch.size();
            } catch (Exception e) {
                LOGGER.severe("Exception while persisting videos: " + e);
                throw e;
            } finally {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
            }
        }

        LOGGER.info("Successfully persisted " + persistedVideos + " videos");
    }

    /**
     * As we face serious issues regarding the daily quota of data store writes, we have to economize every write call.
     * Therefore we check if a video is already existing and remove it from the list that is going to be persisted.
     */
    private static void cleanExistingVideos(PersistenceManager pm, List<Video> videos) {
        for(Video video : videos) {
            try {
                if(pm.getObjectById(Video.class, video.getKey()) != null) {
                    // video already exists
                    videos.remove(video);
                }
            } catch (Exception ignore) {
                // DataNucleus throws an exception if it cannot find the object - in that case we do nothing
            }
        }
    }


    public static void persistVideo(Video video) throws Exception {
        LOGGER.info("Persisting video " + video.getFileName());
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
        LOGGER.info("Indexing " + videos.size() + " videos");
        int trials = 0;

        while(true) {
            trials ++;
            try {
                List<Document> documents = new ArrayList<>();
                for (Video video : videos) {
                    if (video.getTrajectory() != null) {
                        documents.add(createDocument(video));
                    }
                }

                indexDocuments(documents);
                LOGGER.info("Successfully indexed the videos");
                return;
            } catch(Exception e) {
                if(e instanceof ApiProxy.ApiDeadlineExceededException && trials < 3) {
                    LOGGER.warning("ApiDeadlineExceededException while indexing videos. No worries, we're trying again!");
                } else {
                    LOGGER.severe("Exception while indexing videos: " + e);
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }

    private static Document createDocument(Video video) throws Exception {
        try {
            String docId = video.getFileName();
            Trajectory trajectory = video.getTrajectory();
            GeoPoint maxPoint = trajectory.getMaxLocation().toGeoPoint();
            GeoPoint minPoint = trajectory.getMinLocation().toGeoPoint();
            GeoPoint centerPoint = trajectory.getCenter().toGeoPoint();
            long searchRange = trajectory.getSearchRange();

            return Document.newBuilder().setId(docId)
                .addField(Field.newBuilder().setName("maxPoint").setGeoPoint(maxPoint))
                .addField(Field.newBuilder().setName("minPoint").setGeoPoint(minPoint))
                .addField(Field.newBuilder().setName("centerPoint").setGeoPoint(centerPoint))
                .addField(Field.newBuilder().setName("searchRange").setNumber(searchRange))
                .build();
        } catch (Exception e) {
            LOGGER.severe("Exception while creating document for video: " + e);
            throw e;
        }
    }

    private static void indexDocuments(List<Document> documents) throws Exception {
        try {
            Index index = getIndex();

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

    private static Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_VIDEOS).build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    private static Video getVideo(Document document) throws Exception {
        String fileName = document.getId();
        Key key = KeyFactory.createKey(Video.class.getSimpleName(), fileName);
        Video video = getVideo(key);
        if(video == null) {
            LOGGER.warning("Video not found by key " + key);
        }
        return video;
    }

    private static String geoPoint(Location location) {
        return "geopoint(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    private static long getMaxSearchRange() {
        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression("searchRange")
                        .setDirection(SortExpression.SortDirection.DESCENDING)
                        .setDefaultValueNumeric(99999999.00))
                .setLimit(RESULT_LIMIT)
                .build();


        Query query =   Query.newBuilder()
                .setOptions(QueryOptions.newBuilder()
                        .setSortOptions(sortOptions)
                        .setNumberFoundAccuracy(1)
                        .setReturningIdsOnly(false)
                        .setLimit(1).build())  // return only 1 document (the one with the maximum search range)
                .build("searchRange > 0"); // some dummy query to select all documents


        Results<ScoredDocument> results = getIndex().search(query);
        if(results != null && results.getNumberFound() > 0) {
            for(Document document : results) {
                return Math.round(document.getOnlyField("searchRange").getNumber());
            }
        }

        return 0;
    }
}
