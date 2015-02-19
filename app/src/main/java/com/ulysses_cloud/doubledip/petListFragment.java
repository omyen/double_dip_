package com.ulysses_cloud.doubledip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.ulysses_cloud.doubledip.dummy.DummyContent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class petListFragment extends Fragment implements AbsListView.OnItemClickListener {

    public static final String loginURL= "http://www.ulysses-cloud.com/doubleDip/petList.php";
    public static final String TAG = "petListFragment";/**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private GetPetListTask mGetPetsTask = null;

    // UI references.
    private View mProgressView;

    private String phpResult= null;
    private boolean phpError= true;
    private JSONArray pets= null;
    private String token= null;

    private OnFragmentInteractionListener mListener;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private TextView mTextView;

    private static final String ARG_PARAM1 = "param1";

    private List<Pet> listOfPets;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private PetListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static petListFragment newInstance(String param1) {
        petListFragment fragment = new petListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public petListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            token = getArguments().getString(ARG_PARAM1);
            Log.i(TAG,token);
        }

        listOfPets = new ArrayList<>();
        mAdapter = new PetListAdapter(getActivity(),
                android.R.layout.simple_list_item_1 ,listOfPets);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        mTextView = (TextView) view.findViewById(R.id.listEmpty);
        mProgressView = view.findViewById(R.id.get_pets_progress);
        attemptGetPets();
        return view;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public boolean parseJson(String result){

        JSONObject jObject;

        try {
            jObject = new JSONObject(result);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error: failed to make object");
            return false;
        }

        try {
            phpResult = jObject.getString("result");
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error: no result key");
            return false;
        }

        try {
            phpError = jObject.getBoolean("error");
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error: no error code");
            return false;
        }

        if(phpResult.equals("success")) {
            try {
                pets = jObject.getJSONArray("pets");
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error: no pets list");
                return false;
            }
        }

        return true;

    }

    public boolean parsePets(){
        JSONObject jObject;
        Pet petToAdd = new Pet();
        Log.i(TAG, "Number of pets: " + pets.length());
        for(int i=0;i<pets.length();i++){
            try {
                jObject = pets.getJSONObject(i);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error: couldn't make object from pet.");
                return false;
            }
            try {
                petToAdd.petID = jObject.getString("petID");
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error: no petID in pet");
                return false;
            }
            try {
                petToAdd.petName = jObject.getString("petName");
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error: no petName in pet");
                return false;
            }
            try {
                petToAdd.lastFed = jObject.getLong("lastFed");
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error: no lastFed in pet");
                return false;
            }

            listOfPets.add(petToAdd);

        }

        return true;
    }

    public boolean parseGetPetList(String result){
        Log.i(TAG, result);
        if (!parseJson(result)){
            Log.e(TAG, "JSON Error");
            //mTextView.setError(getString(R.string.error_error));
            return false;
        }
        if (phpError==true){
            Log.e(TAG, "PHP Error");
            Log.e(TAG, "PHP Result: " + phpResult);
            //mTextView.setError(getString(R.string.error_error));
            return false;
        }

        if(phpResult.equals("noRecords")){
            Log.i(TAG, "No Email");
            return false;
        }

        if(phpResult.equals("badToken")){
            Log.i(TAG, "Bad Login");
            //mTextView.setError(getString(R.string.error_incorrect_password));
            return false;
        }

        if(phpResult.equals("success")){
            Log.i(TAG, "Pet list retrieval success");
            if(parsePets()) {
                return true;
            } else {
                return false;
            }
        }


       // mTextView.setError(getString(R.string.error_error));
        return false;

    }


    public void attemptGetPets() {
        if (mGetPetsTask != null) {
            return;
        }

        showProgress(true);
        mGetPetsTask = new GetPetListTask(token);
        mGetPetsTask.execute((Void) null);
    }


    public class GetPetListTask extends AsyncTask<Void, Void, Boolean> {
        private final String token;

        GetPetListTask(String theToken) {
            token= theToken;
            pets= null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, loginURL);
            return parseGetPetList(doPetGet(token));
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetPetsTask = null;
            showProgress(false);

            if (success) {
                if(listOfPets.size()==0) {//if no pets
                    mListView.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                } else {//if pets
                    //notify the ui that the data has changed
                    mAdapter.notifyDataSetChanged();
                    mListView.setVisibility(View.VISIBLE);
                    mTextView.setVisibility(View.GONE);
                }
            } else {
                mListView.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled() {
            mGetPetsTask = null;
            showProgress(false);
        }
    }

    private String doPetGet(String theToken){
        String targetURL= loginURL + "?{\"token\":\"" + theToken + "\"}";
        Log.i(TAG, targetURL);
        try {
            return loadFromNetwork(targetURL);
        } catch (IOException e) {

        }
        return null;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        // BEGIN_INCLUDE(get_inputstream)
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
        // END_INCLUDE(get_inputstream)
    }

    private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    /** Initiates the fetch operation. */
    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        String str ="";

        try {
            stream = downloadUrl(urlString);
            str = readIt(stream, 500);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
