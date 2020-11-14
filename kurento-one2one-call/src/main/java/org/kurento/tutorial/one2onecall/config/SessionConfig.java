package org.kurento.tutorial.one2onecall.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.kurento.tutorial.one2onecall.dynamodb.dao.MeetingRoomDAO;
import org.kurento.tutorial.one2onecall.room.RoomManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

	@Bean
	public RoomManager roomManager() {
		return new RoomManager();
	}

	@Bean
	public AmazonDynamoDB amazonDynamoDB() {
		return AmazonDynamoDBClientBuilder.standard()
			.withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
			.build();
	}

	@Bean
	public DynamoDBMapper getDynamoDBMapper() {
		AmazonDynamoDB amazonDynamoDB = amazonDynamoDB();
		return new DynamoDBMapper(amazonDynamoDB);
	}

	@Bean
	public MeetingRoomDAO meetingRoomDAO() {
		return new MeetingRoomDAO(getDynamoDBMapper());
	}

}
