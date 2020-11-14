package org.kurento.tutorial.one2onecall.dynamodb.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "AppointmentScheduleRecord")
public class AppointmentScheduleRecord {

    private String patientName;

    private String providerName;

    private Date time;

    @DynamoDBHashKey
    public String getProviderName() {
        return providerName;
    }

    @DynamoDBIndexHashKey
    public String getPatientName() {
        return patientName;
    }
}
