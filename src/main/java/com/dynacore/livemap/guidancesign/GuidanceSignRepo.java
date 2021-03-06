package com.dynacore.livemap.guidancesign;

import com.dynacore.livemap.core.repo.JpaRepository;
import com.dynacore.livemap.guidancesign.entity.GuidanceSignEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Profile("guidancesign")
@Repository("guidanceSignRepository")
public class GuidanceSignRepo implements JpaRepository<GuidanceSignEntity> {
    private final Logger logger = LoggerFactory.getLogger(GuidanceSignRepo.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void save(GuidanceSignEntity guidanceSignEntity) {
        try {

            if (!entityManager.contains(guidanceSignEntity)) {
                entityManager.persist(guidanceSignEntity);
                guidanceSignEntity.getInnerDisplays().stream().forEach(display -> entityManager.persist(display));
                entityManager.flush();
            } else {
                logger.info(guidanceSignEntity.getName() + "already written to db. The date and time we retrieved it from provider: " + guidanceSignEntity.getRetrievedFromThirdParty() + " \n DateTime when it was published to the provider: " + guidanceSignEntity.getPubDate());
            }

        } catch (PersistenceException error) {
            logger.error("Error, could not write to DB: " + error);
        }
    }

    @Override
    public Optional<GuidanceSignEntity> get(long id) {
        return Optional.ofNullable(entityManager.find(GuidanceSignEntity.class, id));
    }

    @Override
    public List<GuidanceSignEntity> getAll() {
        Query query = entityManager.createQuery("");
        return query.getResultList();
    }

    @Override
    public void update(GuidanceSignEntity guidanceSignEntityLogData, String[] params) {
    }

    @Override
    public void delete(GuidanceSignEntity guidanceSignEntityLogData) {
    }
}
