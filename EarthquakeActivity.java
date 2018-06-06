/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<ArrayList<EarthQuakeData>> {

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;


    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */

    private static final int EARTHQUAKE_lOADER_ID = 1;

    /** Adapter for the list of earthquakes */
    private QuakeAdapter adapter;

    /** URL for earthquake data from the USGS dataset */
    private static final String USGS_REQUEST_URl = "https://earthquake.usgs.gov/fdsnws/event/1/query";


    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Create a new {@link ArrayAdapter} of earthquakes
        adapter = new QuakeAdapter(this, new ArrayList<EarthQuakeData>());


        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);

        //get reference to the Connectivity manager to check state of network
        ConnectivityManager connMgr=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //get details on currently active default data network
        NetworkInfo networkInfo=connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if(networkInfo!=null&&networkInfo.isConnected())
        {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_lOADER_ID, null, this);
        }
        else
        {
            //otherwise display error
            //first ,hide loading indicator so error message will be visible
            View loadingIndicator=findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);
            //update empty stte with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }





        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                EarthQuakeData currentEarthquake = (EarthQuakeData) adapter.getItem(position);

                Uri earthquakeUri=Uri.parse(currentEarthquake.getUrl());

                Intent websiteIntent = new Intent(Intent.ACTION_VIEW,earthquakeUri);

                startActivity(websiteIntent);
            }
        });


    }

    @Override
    public Loader<ArrayList<EarthQuakeData>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude=sharedPreferences.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));



        Uri baseUri=Uri.parse(USGS_REQUEST_URl);
        Uri.Builder uriBuiler =baseUri.buildUpon();

        uriBuiler.appendQueryParameter("format","geojson");
        uriBuiler.appendQueryParameter("limit","10");
        uriBuiler.appendQueryParameter("minmag",minMagnitude);
        uriBuiler.appendQueryParameter("orderby","time");
        return new EarthquakeLoader(this, uriBuiler.toString());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<EarthQuakeData>> loader, ArrayList<EarthQuakeData> eathquakes)
    {
        ProgressBar progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_earthquakes);

        // Clear the adapter of previous earthquake data
        adapter.clear();
        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if(eathquakes!=null && !eathquakes.isEmpty())
        {
            adapter.addAll(eathquakes);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<EarthQuakeData>> loader)
    {
        //Loader reset, so we can clear out our exiting data.
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
//MENU
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_settings)
        {
            Intent settingsIntent=new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
