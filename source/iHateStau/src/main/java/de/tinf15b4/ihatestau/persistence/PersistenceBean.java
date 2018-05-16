package de.tinf15b4.ihatestau.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;

@ApplicationScoped
public class PersistenceBean {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceBean.class);
	private EntityManagerFactory factory;

	public PersistenceBean() {
		factory = Persistence.createEntityManagerFactory("production");
	}

	@VisibleForTesting
	public PersistenceBean(String persistenceUnit) {
		factory = Persistence.createEntityManagerFactory(persistenceUnit);
	}

	public void persist(Object entity) {
		logger.debug("Persisting entity {}", entity);
		Stopwatch stp = Stopwatch.createStarted();
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
			logger.debug("Finished persisting entity {} in {}ms", entity, stp.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	public <T> T merge(T entity) {
		logger.debug("Merging entity {}", entity);
		Stopwatch stp = Stopwatch.createStarted();
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			T updatedEntity = em.merge(entity);
			em.getTransaction().commit();
			return updatedEntity;
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
			logger.debug("Finished merging entity {} in {}ms", entity, stp.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	public void delete(Object entity) {
		logger.debug("Deleting entity {}", entity);
		Stopwatch stp = Stopwatch.createStarted();
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			entity = em.merge(entity);
			em.remove(entity);
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
			logger.debug("Finished deleting entity {} in {}ms", entity, stp.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> select(String statement, Map<String, Object> parameters) {
		logger.debug("Selecting {}", statement);
		Stopwatch stp = Stopwatch.createStarted();
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery(statement);
			for (Entry<String, Object> keyValue : parameters.entrySet()) {
				query.setParameter(keyValue.getKey(), keyValue.getValue());
			}
			List<T> result = query.getResultList();
			em.getTransaction().commit();
			return result;
		} catch (PersistenceException e) {
			logger.error("Query operation failed", e);
			em.getTransaction().rollback();
			return new ArrayList<>();
		} catch (IllegalArgumentException e) {
			logger.error("This parameter does not exist or the query string is not valid", e);
			return new ArrayList<>();
		} finally {
			em.close();
			logger.debug("Finished selecting {} in {}ms", statement, stp.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectAll(Class<T> expectedResult) {
		logger.debug("Selecting all {}", expectedResult.getSimpleName());
		Stopwatch stp = Stopwatch.createStarted();
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery(String.format("SELECT e FROM %s e", expectedResult.getSimpleName()));
			List<T> result = query.getResultList();
			em.getTransaction().commit();
			return result;
		} catch (PersistenceException e) {
			logger.error("Query operation failed", e);
			em.getTransaction().rollback();
			return new ArrayList<>();
		} finally {
			em.close();
			logger.debug("Finished selecting all {} in {}ms", expectedResult.getSimpleName(),
					stp.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	public <T> T selectById(Class<T> expectedResult, Object id) {
		logger.debug("Selecting {} by id {}", expectedResult.getSimpleName(), id);
		EntityManager em = factory.createEntityManager();
		try {
			return em.find(expectedResult, id);
		} finally {
			em.close();
		}
	}

	public void executeNativeQuery(String query) {
		EntityManager em = factory.createEntityManager();
		try {
			em.getTransaction().begin();
			em.createNativeQuery(query).executeUpdate();
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

}
