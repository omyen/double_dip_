package com.ulysses_cloud.doubledip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
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
 * {@link registerUserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link registerUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class registerUserFragment extends Fragment {


    public static final String loginURL= "http://www.ulysses-cloud.com/doubleDip/registerUser.php";
    public static final String TAG = "registerUserFragment";/**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegistrationTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String phpResult= null;
    private boolean phpError= true;
    private String token= null;

    private OnFragmentInteractionListener mListener;



    public static registerUserFragment newInstance() {
        registerUserFragment fragment = new registerUserFragment();

        return fragment;
    }

    public registerUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V= inflater.inflate(R.layout.fragment_register_user, container, false);

        mEmailView = (AutoCompleteTextView) V.findViewById(R.id.registrationEmail);
        mPasswordView = (EditText) V.findViewById(R.id.registrationPassword);
        mConfirmPasswordView = (EditText) V.findViewById(R.id.registrationConfirmPassword);

        mLoginFormView = V.findViewById(R.id.registrationForm);
        mProgressView = V.findViewById(R.id.registrationProgress);
        Button mRegisterButton = (Button) V.findViewById(R.id.registrationButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        return V;
    }

    public void attemptRegistration() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();
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

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(confirmPassword) && !isPasswordValid(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (!doPasswordsMatch(password, confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_passwords_not_match));
            focusView = mConfirmPasswordView;
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
            mAuthTask = new UserRegistrationTask(email, password);
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

    private boolean doPasswordsMatch(String password1, String password2) {
        return password1.equals(password2);
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

        try {
            token = jObject.getString("token");
        } catch (JSONException e) {
        }


        return true;

    }

    public boolean parseRegistration(String result){
        Log.i(TAG, result);
        if (!parseJson(result)){
            Log.e(TAG, "JSON Error");
            mConfirmPasswordView.setError(getString(R.string.error_error));
            return false;
        }
        if (phpError==true){
            Log.e(TAG, "PHP Error");
            Log.e(TAG, "PHP Result: " + phpResult);
            mConfirmPasswordView.setError(getString(R.string.error_error));
            return false;
        }

        if(phpResult.equals("userNameTaken")){
            Log.i(TAG, "userNameTaken");
            mConfirmPasswordView.setError(getString(R.string.error_username_taken));
            return false;
        }

        if(phpResult.equals("registrationSuccess")){
            Log.i(TAG, "Registration success");
            return true;
        }


        mConfirmPasswordView.setError(getString(R.string.error_error));
        return false;

    }

    public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserRegistrationTask(String email, String password) {
            mEmail = email;            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.i(TAG, loginURL);
            return parseRegistration(doRegistrationPost(mEmail,mPassword));
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                mListener.onRegistrationSuccess(mEmail);
            } else {
                mConfirmPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private String doRegistrationPost(String userName, String password){
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        addToListNameValuePair("userName", userName, nameValuePairs);
        // TODO hash password
        addToListNameValuePair("password", password, nameValuePairs);
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

        public void onRegistrationSuccess(String email);
    }

}
