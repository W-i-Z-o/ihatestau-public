package de.tinf15b4.ihatestau.camera;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.tinf15b4.ihatestau.camera.api.CameraImageMasker;
import de.tinf15b4.ihatestau.camera.api.CameraSpotTrafficEvaluationService;
import de.tinf15b4.ihatestau.camera.api.ImageClassifier;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.CameraSpotTrafficStateEntity;
import de.tinf15b4.ihatestau.persistence.ImageMaskEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.persistence.TrafficStateId;
import de.tinf15b4.ihatestau.rest.exceptions.NoDataCachedException;

@ApplicationScoped
public class CameraSpotTrafficEvaluationServiceImpl implements CameraSpotTrafficEvaluationService {
	private static final Logger logger = LoggerFactory.getLogger(CameraSpotTrafficEvaluationServiceImpl.class);

	private static final float EXPONENTIAL_SMOOTHING_ALPHA = 0.2f;

	private final byte[] OUT_OF_SERVICE_SHA1 = new byte[] {
			-53,   1, -31, -57,   52, 122, 63, -91, 78, 72,
			-21, -81, 106, -26, -105, -56, 18,  48, 13, 16 };

	@Inject
	private PersistenceBean persistenceBean;

	@Inject
	private CameraImageMasker cameraImageMasker;

	@Inject
	private ImageClassifier imageClassifier;

	private Cache<CameraSpotConfigEntity, CameraSpotTrafficStateEntity> cache;

	public CameraSpotTrafficEvaluationServiceImpl() {
		// just give a little more than 1 Minute to prevent empty cache. Values should
		// be overriden after 60 seconds so this timeout will only hit if something
		// fails
		cache = CacheBuilder.newBuilder().expireAfterWrite(70, TimeUnit.SECONDS).build();
	}

	@Override
	public void processImage(CameraSpotConfigEntity spotEntity, byte[] imageFront, byte[] imageBack)
			throws IOException {
		TrafficStateId stateId = new TrafficStateId(spotEntity.getId(), new Date());

		float jamProbability = 0.0f;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] da = md.digest(imageFront);
			byte[] db = md.digest(imageBack);

			if (!Arrays.equals(da, OUT_OF_SERVICE_SHA1) && !Arrays.equals(db, OUT_OF_SERVICE_SHA1)) {
				ImageMaskEntity frontMaskEntity = persistenceBean.selectById(ImageMaskEntity.class, spotEntity.getMaskFront());
				ImageMaskEntity backMaskEntity = persistenceBean.selectById(ImageMaskEntity.class, spotEntity.getMaskBack());

				imageFront = cameraImageMasker.applyMask(imageFront, frontMaskEntity != null ? frontMaskEntity.getImageData() : null);
				imageBack = cameraImageMasker.applyMask(imageBack, backMaskEntity != null ? backMaskEntity.getImageData() : null);
				jamProbability = imageClassifier.getJamProbability(imageFront, imageBack);
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		float oldJamProbability = 0.0f;
		CameraSpotTrafficStateEntity old = cache.getIfPresent(spotEntity);
		if (old != null)
			oldJamProbability = old.getJamProbabilitySmooth();

		float newJamProbabilitySmooth = (1-EXPONENTIAL_SMOOTHING_ALPHA)*oldJamProbability + EXPONENTIAL_SMOOTHING_ALPHA*jamProbability;

		CameraSpotTrafficStateEntity trafficStateEntity = new CameraSpotTrafficStateEntity(stateId, jamProbability,
				newJamProbabilitySmooth, imageFront, imageBack);
		persistenceBean.persist(trafficStateEntity);

		cache.put(spotEntity, trafficStateEntity);
	}

	@Override
	public byte[] getImageFront(CameraSpotConfigEntity c) throws NoDataCachedException {
		return getEntry(c).getImageFront();
	}

	@Override
	public byte[] getImageBack(CameraSpotConfigEntity c) throws NoDataCachedException {
		return getEntry(c).getImageBack();
	}

	@Override
	public float getJamProbabilityRaw(CameraSpotConfigEntity c) {
		try {
			return getEntry(c).getJamProbability();
		} catch (NoDataCachedException e) {
			return 0;
		}
	}

	@Override
	public float getJamProbabilitySmooth(CameraSpotConfigEntity c) {
		try {
			return getEntry(c).getJamProbabilitySmooth();
		} catch (NoDataCachedException e) {
			return 0;
		}
	}

	private CameraSpotTrafficStateEntity getEntry(CameraSpotConfigEntity c) throws NoDataCachedException {
		CameraSpotTrafficStateEntity entity = cache.getIfPresent(c);
		if (entity == null)
			throw new NoDataCachedException("There is no cached entry for spot " + c.getName());
		return entity;
	}
}
