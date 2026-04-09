package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.PersistentOAuth2AuthorizedClient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersistentOAuth2AuthorizedClientRepository extends MongoRepository<PersistentOAuth2AuthorizedClient, String> {
    Optional<PersistentOAuth2AuthorizedClient> findByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName);
    
    void deleteByClientRegistrationIdAndPrincipalName(String clientRegistrationId, String principalName);
}
