package com.ulysses_cloud.doubledip;

import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class petListActivity extends FragmentActivity implements petListFragment.OnFragmentInteractionListener{
    public static final String PREFS_NAME = "doubleDipPrefs";
    private String token= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        token= settings.getString("token","noToken");
        Log.i("TAG", token);
        setContentView(R.layout.activity_pet_list);
        // Create a new Fragment to be placed in the activity layout
        petListFragment firstFragment = new petListFragment();
        Bundle args = new Bundle();
        args.putString("param1",token);
        firstFragment.setArguments(args);
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments

        // Add the fragment to the 'fragment_container' FrameLayout
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.pet_list_fragment_container, firstFragment);
        transaction.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pet_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(String id){

    }

}
