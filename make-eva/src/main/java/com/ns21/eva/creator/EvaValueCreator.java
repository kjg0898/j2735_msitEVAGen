package com.ns21.eva.creator;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ns21.common.mist.parser.UuidMatching;
import com.ns21.common.util.MetaDataConvertUtil;

import java.io.IOException;
import java.util.*;

import static com.ns21.common.util.MetaDataConvertUtil.*;
import static com.ns21.eva.creator.EvaMessageCreator.ITISEVACodeGen;

/**
 * packageName    : com.ns21.eva.creator
 * fileName       : EvaMessageCreator.java
 * author         : kjg08
 * date           : 2023-11-24
 * description    : JSON 처리 및 열거형과 코덱 처리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-24        kjg08           최초 생성
 */
public class EvaValueCreator {
    private static int msgCnt = 0;

    public static List<String> createEvaMessage() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> messages = new ArrayList<>();
        // UuidMatching 클래스를 사용하여 관련된 데이터의 JSON 문자열을 가져옵니다.
        List<String> relatedUuids = UuidMatching.UuidPut();

        //  relatedUuids에는 연관된 데이터의 JSON 문자열이 포함되어 있습니다.
        for (String uuidJson : relatedUuids) {
            // JSON 문자열을 Map 객체로 변환합니다.
            Map<String, Object> uuidMap = mapper.readValue(uuidJson, new TypeReference<>() {
            });
            // RSA 메시지를 구성합니다.
            Map<String, Object> evaMessage = new LinkedHashMap<>();
            // uuidMap에서 필요한 데이터를 추출하여 RSA 메시지를 구성합니다.
            // 공통 필드를 추가합니다.
            evaMessage.put("msgCnt", msgCnt);
            msgCnt = (msgCnt + 1) % 128;  // msgCnt가 127을 넘지 않도록 함
            // Double 형태로 변환한 후 longValue() 메소드를 사용
            String timestampStr = String.valueOf(uuidMap.get("egoPose_timestamp"));
            long timestamp = (timestampStr != null && !timestampStr.isEmpty()) ? (long) Double.parseDouble(timestampStr) : 0;
            int minutesOfYear = minuteOfTheYear(Long.toString(timestamp));
            evaMessage.put("timeStamp", minutesOfYear);

            // "description" 필드에 ITIScode 값을 할당합니다. 주의할 차량(itis 코드)추가  instance.json 의 category_name
            evaMessage.put("typeEvent", 0); //주변 차량과 itis 코드 필요 instance.json 의 category_name
            // Determine ITIS codes for the message
            String categoryName = uuidMap.containsKey("instance_categoryName") ? (String) uuidMap.get("instance_categoryName") : "0";
            List<Integer> descriptionValues = getDescriptionValuesFromSomeSource(categoryName);
            // categoryName 또는 vehicleState가 유효하지 않은 경우 이 메시지를 건너뛰거나 생략함. "description"
            if (descriptionValues.isEmpty()) {
                continue; // 현재 생성을 건너뛰고 다음 메세지를 생성합니다.
            }
            evaMessage.put("description", descriptionValues);
            // 여기까지 ITIS 코드 추가 로직

            evaMessage.put("priority", "00");
            // 위치 및 방향 정보를 추가합니다.
            Map<String, Object> position = new LinkedHashMap<>();

            // 위치 및 방향 정보를 추가하는 부분에서의 타입 안전성 확인
            if (uuidMap.get("egoPose_translation") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Double> translation = (List<Double>) uuidMap.get("egoPose_translation");
                double utmX = translation.get(0); // ex)326865.27824246883
                double utmY = translation.get(1); // ex)4147694.5101196766
                int utmZone = 52; //대한민국 대부분 지역은 52 (일부51)
                double elevation = translation.get(2); // ex)49.126053147017956
                long[] utmToLatLon = MetaDataConvertUtil.CoordinateConverter.utmToLatLon(utmX, utmY, utmZone, elevation);

                // convertTimestampToUtcMap에는 문자열 형태의 타임스탬프를 전달
                position.put("utcTime", convertTimestampToUtcMap(Long.toString(timestamp)));
                position.put("long", utmToLatLon[1]);  // ego_pose.json 파일의 translation,
                position.put("lat", utmToLatLon[0]);  // ego_pose.json 파일의 translation,
                position.put("elevation", utmToLatLon[2]);  // ego_pose.json 파일의 translation,
                evaMessage.put("position", position);

                // 추가적인 지역 정보를 구성합니다.
                List<Map<String, Object>> regional = new ArrayList<>();
                Map<String, Object> regionalItem = new LinkedHashMap<>();
                regionalItem.put("regionId", 4); // 예시 regionId

                Map<String, Object> regExtValue = new LinkedHashMap<>();
                Map<String, Object> cits = new LinkedHashMap<>();
                cits.put("stopID", uuidMap.get("log_location"));
                cits.put("text", uuidMap.get("frameData_uuid"));
                regExtValue.put("cits", cits);
                regionalItem.put("regExtValue", regExtValue);
                regional.add(regionalItem);

                evaMessage.put("regional", regional);

                // 최종적인 RSA 메시지를 구성합니다.
                Map<String, Object> evaMsg = new LinkedHashMap<>();
                evaMsg.put("rsaMsg", evaMessage);
                messages.add(mapper.writeValueAsString(evaMsg));
            }
        }
        return messages; // 여러 메시지를 리스트로 반환
    }

    public static List<Integer> getDescriptionValuesFromSomeSource(String categoryName) {

        // ITIS 코드 변환 함수를 호출하여 descriptionValues를 생성합니다.
        return ITISEVACodeGen(categoryName);
    }
}