package de.scampiRest.data;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestScampiMessageRepository extends MongoRepository<RestScampiMessage, String> {

	List<RestScampiMessage> findByService (String service);
	List<RestScampiMessage> findByAppTag (String appTag);
	
}
