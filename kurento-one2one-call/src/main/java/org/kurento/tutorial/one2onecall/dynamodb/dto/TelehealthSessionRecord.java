package org.kurento.tutorial.one2onecall.dynamodb.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "TelehealthSessionRecord")
public class TelehealthSessionRecord {

    private String patientName;

    private String sessionId;

    @DynamoDBHashKey
    public String getPatientName() {
        return patientName;
    }
}
