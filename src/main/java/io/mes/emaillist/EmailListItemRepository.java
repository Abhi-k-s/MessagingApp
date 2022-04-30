package io.mes.emaillist;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface EmailListItemRepository extends CassandraRepository<EmailListItem, EmailListItemKey>{
  //  List<EmailListItem> findAllById(EmailListItemKey id);

    List<EmailListItem> findAllByKey_IdAndKey_label(String id, String label);
    
}
