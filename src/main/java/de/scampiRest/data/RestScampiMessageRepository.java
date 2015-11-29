package de.scampiRest.data;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestScampiMessageRepository extends MongoRepository<RestScampiMessage, String> {

}
