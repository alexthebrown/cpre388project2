package com.example.project2_subcompanion;

public class CalendarModel {
        private String text;

        private String id, title, date, location,price;

        public CalendarModel(String id,String title, String date, String location, String price) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.location = location;
            this.price = price;
        }

        public String getTitle() {
            return title;
        }
        public String getDate() {
            return date;
        }
        public String getLocation() {
            return location;
        }
        public  String getPrice() {
            return price;
        }
        public String getId() { return id;}
}
