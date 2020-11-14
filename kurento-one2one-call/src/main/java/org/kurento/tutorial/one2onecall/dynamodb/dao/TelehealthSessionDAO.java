package org.kurento.tutorial.one2onecall.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TelehealthSessionDAO {

    private final DynamoDBMapper dynamoDBMapper;


}
