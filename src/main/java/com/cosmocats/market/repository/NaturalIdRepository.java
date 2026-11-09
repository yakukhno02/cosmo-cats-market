package com.cosmocats.market.repository;

import java.io.Serializable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NaturalIdRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    Optional<T> findByNaturalId(ID naturalId);

    void deleteByNaturalId(ID naturalId);
}
