package com.cosmocats.market.repository.impl;

import com.cosmocats.market.repository.NaturalIdRepository;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class NaturalIdRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements NaturalIdRepository<T, ID> {

    private final EntityManager entityManager;

    public NaturalIdRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByNaturalId(ID naturalId) {
        return entityManager.unwrap(Session.class).bySimpleNaturalId(this.getDomainClass())
                .loadOptional(naturalId);
    }

    @Override
    @Transactional
    public void deleteByNaturalId(ID naturalId) {
        findByNaturalId(naturalId).ifPresent(this::delete);
    }
}
