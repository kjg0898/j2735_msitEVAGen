package com.ns21.common.itis;

/**
 * packageName    : com.ns21.common.itis
 * fileName       : ItisCodeMapper.java
 * author         : kjg08
 * date           : 2024-01-08
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-01-08        kjg08           최초 생성
 */
public class ItisCodeMapper {
    //Category 로 ITIS 코드 결정
    public static Integer getCategoryITISCode(String categoryName) {
        // categoryName을 기반으로 ITIS 코드를 반환합니다.
        // 해당 카테고리에 유효한 ITIS 코드가 없으면 null을 반환합니다.
        return switch (categoryName) {
            case "dynamic_object.vehicle.police_car" -> ItisCodes.POLICE_CAR;
            case "dynamic_object.vehicle.ambulance" -> ItisCodes.AMBULANCE;
            case "dynamic_object.vehicle.fire_truck" -> ItisCodes.FIRE_TRUCK;
            case "dynamic_object.human.pedestrian" -> ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.police_officer" -> ItisCodes.POLICE_CAR;
            case "dynamic_object.human.construction_worker" -> ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.firefighter" -> ItisCodes.FIRE_TRUCK;
            case "dynamic_object.human.stroller" -> ItisCodes.PEDESTRIAN_ON_ROAD;
            case "dynamic_object.human.wheelchair" -> ItisCodes.PEDESTRIAN_ON_ROAD;
            case "movable_object.traffic_cone" -> ItisCodes.MOBILE_CONSTRUCTION;
            case "movable_object.barrier" -> ItisCodes.ROAD_CLOSURE_LANE_BLOCKAGE;
            case "movable_object.debris" -> ItisCodes.FALLING_OBJECTS;
            case "movable_object.other_movable_object" -> ItisCodes.FALLING_OBJECTS;
            default -> null;
        };
    }
}