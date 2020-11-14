package org.kurento.tutorial.one2onecall.dynamodb.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "MeetingRoomRecord")
public class MeetingRoomRecord {

    private String patientName;

    private String providerName;

    private String roomName;

    @DynamoDBHashKey
    public String getPatientName() {
        return patientName;
    }
}
