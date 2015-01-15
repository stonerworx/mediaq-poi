package de.lmu.ifi.dbs.mediaqpoi.control;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.google.appengine.api.datastore.Key;

import de.lmu.ifi.dbs.mediaqpoi.entity.Video;

public final class PersistenceFacade {

	private static final Logger LOGGER = Logger.getLogger(PersistenceFacade.class.getName());

	private static final PersistenceManagerFactory pmfInstance = JDOHelper.getPersistenceManagerFactory("transactions-optional");

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
			if (tx.isActive()) {
				tx.rollback();
			}
		}
	}
}