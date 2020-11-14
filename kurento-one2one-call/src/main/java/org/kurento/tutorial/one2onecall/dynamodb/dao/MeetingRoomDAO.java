package org.kurento.tutorial.one2onecall.dynamodb.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.kurento.tutorial.one2onecall.dynamodb.dto.MeetingRoomRecord;

@AllArgsConstructor
public class MeetingRoomDAO {

    private final DynamoDBMapper dynamoDBMapper;

    public void addPatientToMeetingRoom(@NonNull String patientName, @NonNull String roomName) {
        MeetingRoomRecord meetingRoomRecord = new MeetingRoomRecord(patientName, null, roomName);
        dynamoDBMapper.save(meetingRoomRecord);
    }

}
