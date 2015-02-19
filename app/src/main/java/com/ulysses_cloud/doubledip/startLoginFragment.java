package com.ulysses_cloud.doubledip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link startLoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link startLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class startLoginFragment extends Fragment {


    public static final String loginURL= "http://www.ulysses-cloud.com/doubleDip/login.php";
    public static final String TAG = "startLoginFragment";/**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String phpResult= null;
    private boolean phpError= true;
    private String token= null;

    private OnFragmentInteractionListener mListener;

    private static final String ARG_PARAM1 = "param1";
    private String email;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment startLoginFragment.
     */
    public static startLoginFragment newInstance(String param1) {
        startLoginFragment fragment = new startLoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public startLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Started Fragment");
        if (getArguments() != null) {
            email = getArguments().getString(ARG_PARAM1);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V= inflater.inflate(R.layout.fragment_start_login, container, false);

        mEmailView = (AutoCompleteTextView) V.findViewById(R.id.email);
        if(email!=null){
            mEmailView.setText(email);
        }
        mPasswordView = (EditText) V.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mLoginFormView = V.findViewById(R.id.login_form);
        mProgressView = V.findViewById(R.id.login_progress);
        Button mEmailSignInButton = (Button) V.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mRegisterButton = (Button) V.findViewById(R.id.registration_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRegisterButton();
            }
        });

        return V;
    }

    public void setEmail(String email){
        if(mEmailView!=null) {
            mEmailView.setText(email);
        }
    }


    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        Log.i(TAG, email);
        Log.i(TAG, password);

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //dont care
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
                token = jObject.getString("token");
            } catch (JSONException e) {
                Log.i(TAG, "JSON Error: no token");
                return false;
            }
        }

        return true;

    }

    public boolean parseLogin(String result){
        Log.i(TAG, result);
        if (!parseJson(result)){
            Log.e(TAG, "JSON Error");
            mPasswordView.setError(getString(R.string.error_error));
            return false;
        }
        if (phpError==true){
            Log.e(TAG, "PHP Error");
            Log.e(TAG, "PHP Result: " + phpResult);
            mPasswordView.setError(getString(R.string.error_error));
            return false;
        }

        if(phpResult.equals("noRecords")){
            Log.i(TAG, "No Email");
            mPasswordView.setError(getString(R.string.error_email_not_found));
            return false;
        }

        if(phpResult.equals("badLogin")){
            Log.i(TAG, "Bad Login");
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            return false;
        }

        if(phpResult.equals("success")){
            Log.i(TAG, "Login success");
            return true;
        }


        mPasswordView.setError(getString(R.string.error_error));
        return false;

    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, loginURL);
            return parseLogin(doLoginPost(mEmail,mPassword));
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                mListener.onLoginSuccess(token);
            } else {
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private String doLoginPost(String userName, String passwordHashed){
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        addToListNameValuePair("userName", userName, nameValuePairs);
        // TODO hash password
        addToListNameValuePair("password", passwordHashed, nameValuePairs);
        HttpResponse response = postData(loginURL, nameValuePairs);
        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity);
        } catch (IOException e){
            return null;
        }
    }

    private void addToListNameValuePair(String name, String value, List<NameValuePair> list){
        list.add(new BasicNameValuePair(name, value));
    }

    private HttpResponse postData(String urlString, List<NameValuePair> postData) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(urlString);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(postData));

            // Execute HTTP Post Request
            return httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
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
        public void onLoginSuccess(String token);
        public void onRegisterButton();
    }

}
