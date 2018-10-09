package com.cyclist.logic.search;

public class Profile {

    public enum RoadType {
        CYCLEWAY(0),
        SIDEWALK(1),
        ROAD(2);

        private int position;

        RoadType(int position){
            this.position = position;
        }

        public int getValue() {
            return position;
        }

        public static RoadType getByPosition(int position){
            for(RoadType roadType : RoadType.values()){
                if(roadType.getValue() == position){
                    return roadType;
                }
            }
            return null;
        }
    }

    public enum RouteMethod {
        SAFEST(0),
        FASTEST(1);

        private int position;

        RouteMethod(int position){
            this.position = position;
        }

        public int getValue() {
            return position;
        }

        public static RouteMethod getByPosition(int position){
            for(RouteMethod routeMethod : RouteMethod.values()){
                if(routeMethod.getValue() == position){
                    return routeMethod;
                }
            }
            return null;
        }
    }
}
