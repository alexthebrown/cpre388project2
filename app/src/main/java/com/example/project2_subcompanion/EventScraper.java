package com.example.project2_subcompanion;

import java.util.ArrayList;
import java.util.List;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Brown & Blake Hardy
 * Failed attempt to make an event scraper
 */
public class EventScraper {

    public List<Map<String, String>> scrapeEvents(String url) {
        List<Map<String, String>> events = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").get();
            Log.d("EVERYTHING", doc.html());

            // Select all events
//            Elements eventElements = doc.select(".page");

            Elements eventElements = doc.select(".event-listing");
            Log.d("CHECKPOINT", eventElements.text());
            for (Element event : eventElements) {

                Map<String, String> eventDetails = new HashMap<>();

                // Extract details
                String eventId = event.attr("id");
                String imageUrl = event.select(".event-listing__image img").attr("src");
                String title = event.select(".event-listing__title h4").text();
//                String title = event.select(".page-title a").text();

                String date = event.select(".event-listing__date").text();
                String time = event.select(".event-listing__time").text();
                String location = event.select(".event-listing__location").text();
                String description = event.select(".event-listing__desc").text();
                String tags = event.select(".event-listing__tags span").text();
                Log.d("CHECKPOINT", title);
                // Store in map
                eventDetails.put("id", eventId);
                eventDetails.put("name", title);
                eventDetails.put("imageUrl", imageUrl);
//                eventDetails.put("date", fullDate);
//                eventDetails.put("time", time);
                eventDetails.put("location", location);
                eventDetails.put("description", description);
//                eventDetails.put("tags", tags);

                events.add(eventDetails);
            }

        } catch (Exception e) {
            Log.e("SCRAPING FAIL", "AWW NOOO", e);
        }
        return events;
    }
}
