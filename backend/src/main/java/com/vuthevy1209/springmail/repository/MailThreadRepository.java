package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailThread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MailThreadRepository extends MongoRepository<MailThread, String> {

	List<MailThread> findByUserId(String userId);

	/** Tất cả threads của user, sắp xếp mới → cũ */
	List<MailThread> findByUserIdOrderByLastMessageTimestampDesc(String userId);

	/**
	 * Threads của user có chứa bất kỳ labelId nào trong collection, sắp xếp mới → cũ.
	 */
	List<MailThread> findByUserIdAndLabelIdsInOrderByLastMessageTimestampDesc(
			String userId, Collection<String> labelIds);
}

