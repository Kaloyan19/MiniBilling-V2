package com.example.minibilling.repository.jpa;

import com.example.minibilling.model.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceEntityRepository extends JpaRepository<InvoiceEntity, String>{
    @Query("SELECT i FROM InvoiceEntity i WHERE i.user.reference = :reference AND i.period = :period")
    InvoiceEntity findByUserReferenceAndPeriod(@Param("reference") String reference, @Param("period") String period);


    java.util.Optional<InvoiceEntity> findFirstByNumber(String number);

    @Query("SELECT i FROM InvoiceEntity i WHERE i.user.reference = :reference")
    List<InvoiceEntity> findByUserReference(@Param("reference") String reference);
}
