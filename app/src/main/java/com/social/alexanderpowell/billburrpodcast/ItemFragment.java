package com.social.alexanderpowell.billburrpodcast;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.social.alexanderpowell.billburrpodcast.dummy.DummyContent;
import com.social.alexanderpowell.billburrpodcast.dummy.DummyContent.DummyItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    //private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private MyItemRecyclerViewAdapter adapter;
    private List<DummyItem> list;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(mLayoutManager);
            //
            list = new ArrayList<DummyItem>();
            //
            adapter = new MyItemRecyclerViewAdapter(list, mListener);
            recyclerView.setAdapter(adapter);
            new FetchFeedTask().execute((Void) null);

            recyclerView.setNestedScrollingEnabled(false);

            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation());
            recyclerView.addItemDecoration(mDividerItemDecoration);
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }

    ///////////////////////////////////////////////////////////////

    private enum RSSXMLTag {
        TITLE, DESCRIPTION, DATE, LINK, IGNORETAG;
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;
        private RSSXMLTag currentTag;

        private List<RssFeedModel> episodes = null;

        @Override
        protected void onPreExecute() {
            urlLink = "https://rss.art19.com/monday-morning-podcast";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                List<RssFeedModel> items = parseFeed(inputStream);
                for (int i = 0; i < items.size(); i++) {
                    Log.d("doInBackground", items.get(i).getLink());
                }
                episodes = items;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (episodes != null) {
                list.clear();
                for (int i = 0; i < episodes.size(); i++) {
                    list.add(new DummyItem(episodes.get(i).getTitle(), episodes.get(i).getDescription(), episodes.get(i).getLink()));
                }
                adapter.notifyDataSetChanged();
            }
        }

        public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
                IOException {
            List<RssFeedModel> items = new ArrayList<>();

            try {
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                xmlPullParser.setInput(inputStream, null);

                int eventType = xmlPullParser.getEventType();
                int count = 0;
                int quota = 5;
                RssFeedModel rssFeedModel = null;
                while (eventType != XmlPullParser.END_DOCUMENT && count < quota) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {
                        if (xmlPullParser.getName().equals("item")) {
                            rssFeedModel = new RssFeedModel();
                            currentTag = RSSXMLTag.IGNORETAG;
                        } else if (xmlPullParser.getName().equals("title")) {
                            currentTag = RSSXMLTag.TITLE;
                        } else if (xmlPullParser.getName().equals("description")) {
                            currentTag = RSSXMLTag.DESCRIPTION;
                        } else if (xmlPullParser.getName().equals("pubDate")) {
                            currentTag = RSSXMLTag.DATE;
                        } else if (xmlPullParser.getName().equals("enclosure")) {
                            currentTag = RSSXMLTag.LINK;
                            String link = xmlPullParser.getAttributeValue(null, "url");
                            rssFeedModel.setLink(link);
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xmlPullParser.getName().equals("item")) {
                            if (rssFeedModel != null) {
                                items.add(rssFeedModel);
                            }
                            count++;
                        } else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        String content = xmlPullParser.getText().trim();
                        if (rssFeedModel != null) {
                            switch (currentTag) {
                                case TITLE:
                                    rssFeedModel.setTitle(content);
                                    break;
                                case DESCRIPTION:
                                    rssFeedModel.setDescription(content);
                                    break;
                                case DATE:
                                    rssFeedModel.setPubDate(content);
                                    break;
                            }
                        }
                    }
                    eventType = xmlPullParser.next();
                    //count++;
                }
                return items;
            } finally {
                inputStream.close();
            }
        }
    }

    public static class RssFeedModel {

        public String title;
        public String link;
        public String description;
        public String pubDate;

        public RssFeedModel() { }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getLink() {
            return this.link;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public String printModel() {
            return this.title + " : " + this.description + " : " + this.pubDate;
        }
    }
}
