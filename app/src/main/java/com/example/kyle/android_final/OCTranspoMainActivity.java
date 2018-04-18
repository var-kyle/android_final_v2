package com.example.kyle.android_final;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class OCTranspoMainActivity extends Activity {

    private static final String APPID = "223eb5c3";
    private static final String APIKEY = "ab27db5b435b8c8819ffb8095328e775";

    private Button btn;
    private EditText editTxt;
    private ListView stopInfo;
    private ArrayList<String> busInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octranspo_main_activity);

        btn = this.findViewById(R.id.btn_search);
        editTxt = this.findViewById(R.id.stop_num);
        stopInfo = this.findViewById(R.id.stop_info);
        busInfo = new ArrayList<>();

        final StopNumberAdapter myAdapter = new StopNumberAdapter();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //https://api.octranspo1.com/v1.2/GetRouteSummaryForStop?appID=223eb5c3&&apiKey=ab27db5b435b8c8819ffb8095328e775&stopNo=3050
                String stopNum = editTxt.getText().toString();
                String stopUrl = "https://api.octranspo1.com/v1.2/GetRouteSummaryForStop?appID=223eb5c3&&apiKey=ab27db5b435b8c8819ffb8095328e775&stopNo=" + stopNum;

                AsyncBusInfo task = new AsyncBusInfo();
                task.execute(stopUrl);

                myAdapter.notifyDataSetChanged();
            }
        });
    }

    public class AsyncBusInfo extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String siteUrl = strings[0];
            String result = "";
            try {
                URL url = new URL(siteUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inStream = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( inStream  , "UTF-8");

                int eventType = xpp.getEventType();
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    switch(eventType)
                    {
                        case XmlPullParser.START_TAG:
                            String tagName = xpp.getName();
                            if(tagName.equals("RouteNo"))
                            {
                                xpp.next();
                                String txt = xpp.getText();
                                result += txt;
                            }
                            if(tagName.equals("Direction"))
                            {
                                xpp.next();
                                String txt = xpp.getText();
                                result += " - " + txt;
                            }
                            break;
                    }
                    busInfo.add(result);
                    xpp.next();
                    eventType = xpp.getEventType();
                }



            } catch (MalformedURLException ex) {

            } catch (IOException ex) {

            } catch (XmlPullParserException ex) {

            }

            return result;
        }
    }

    public class StopNumberAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return stopInfo.getCount();
        }

        @Override
        public View getView(int position, View oldView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View createdRow = oldView;
            if(oldView == null)
                createdRow = inflater.inflate(R.layout.custom_row, parent, false);

            final ViewGroup p = parent;

            TextView tv = (TextView) createdRow.findViewById(R.id.stop_desc);  //look at setContentView in onCreate
            Button rowButton = (Button)createdRow.findViewById(R.id.stop_fav);
            rowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(p.getContext(), "Favourited!", Toast.LENGTH_SHORT).show();
                }
            });
            // if(oldView == null)
            tv.setText( getItem( position ));
            return createdRow;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public String getItem(int position) {
            return busInfo.get(position);
        }
    }
}
