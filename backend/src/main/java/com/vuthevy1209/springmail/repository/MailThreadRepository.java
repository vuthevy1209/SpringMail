package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.MailThread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailThreadRepository extends MongoRepository<MailThread, String> {

	List<MailThread> findByUserId(String userId);

	/** Tất cả threads của user, sắp xếp mới → cũ */
	List<MailThread> findByUserIdOrderByLastMessageTimestampDesc(String userId);

	/**
	 * Threads của user có chứa labelId chỉ định, sắp xếp mới → cũ.
	 * Dùng để filter inbox / sent / category, v.v.
	 */
	List<MailThread> findByUserIdAndLabelIdsContainingOrderByLastMessageTimestampDesc(
			String userId, String labelId);
}

