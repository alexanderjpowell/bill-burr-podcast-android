package com.social.alexanderpowell.billburrpodcast.dummy;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyContent {

    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }

        //new FetchFeedTask().execute((Void) null);
        Log.d("constructor", "constructor");
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.title, item);
    }

    private static DummyItem createDummyItem(int position) {
        //return new DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
        return new DummyItem("Mon, 25 May 2020", "Bill rambles about beach goers, Sharknado movies, and putting stuff to bed.", makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String title;
        public final String description;
        public final String url;

        public DummyItem(String title, String description, String url) {
            this.title = title;
            this.description = description;
            this.url = url;
        }

        @Override
        public String toString() {
            return this.description;
        }
    }
}
