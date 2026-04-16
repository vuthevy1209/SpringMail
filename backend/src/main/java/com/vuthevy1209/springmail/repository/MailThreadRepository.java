package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MailThreadRepository extends MongoRepository<MailThread, String> {

	Page<MailThread> findByUserId(String userId, Pageable pageable);

	@Query("{ 'userId': ?0, 'labelIds': { $all: ?1 } }")
	Page<MailThread> findByUserIdAndLabelIdsContainsAll(String userId, List<String> labelIds, Pageable pageable);

	Page<MailThread> findByUserIdAndIdIn(String userId, List<String> threadIds, Pageable pageable);

	Optional<MailThread> findByIdAndUserId(String id, String userId);

}
