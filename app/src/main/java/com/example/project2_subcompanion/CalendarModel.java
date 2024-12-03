package com.example.project2_subcompanion;

public class CalendarModel {
        private String text;

        private String title, date, location,price;

        public CalendarModel(String title, String date, String location, String price) {
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
}
