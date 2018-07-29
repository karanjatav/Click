package com.example.darkknight.click

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.JsonToken
import android.util.Log
import android.widget.Toast
import com.example.darkknight.click.Extentions.*
import com.example.darkknight.click.POJOs.UserObject
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*


class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    lateinit var callbackManager: CallbackManager

    private val EMAIL = "email"

    var WEB_CLIENT_ID = "95278996686-el16osdnhn995rtajcik25igv7hvhdff.apps.googleusercontent.com"

    private var mGoogleApiClient: GoogleApiClient? = null

    var REQUEST_CODE_SIGN_IN = 123

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    /*    iv_logo.setOnClickListener {
            et_email_login.setText("karanjatav95@gmail.com")
            et_password_login.setText("99999999")
        }*/

        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        iv_fb_login.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("email", "public_profile"))
        }
        fbLogin()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mAuth = FirebaseAuth.getInstance()

        iv_google_login.setOnClickListener {
            googleSignIn()
        }

        btn_login.setOnClickListener { login() }

        btn_register_login.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))

        }
    }

    private fun login() {
        val email = et_email_login.text.toString()
        val password = et_password_login.text.toString()

        if (!TextUtils.isEmpty(email)) {
            if (dbHelper.getUserByEmail(email) != null) {
                val user = dbHelper.getUserByEmail(email)
                if (dbHelper.getUserByEmail(email)!!.password == password) {
                    saveUser(user!!.userId, user.name, user.email, user.profile_pic, user.socialId)
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                } else {
                    toastShort("Password Mismatch")
                }
            } else {
                toastShort("Email Id not found")
            }
        } else {
            toastShort("Please enter your Email Id")
        }
    }


    //FACEBOOK SIGN IN-----------------------------------------------------------------------------------------------------------------------

    fun fbLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val accessToken = AccessToken.getCurrentAccessToken()
                val request = GraphRequest.newMeRequest(accessToken) { data, _ ->
                    try {
                        if (data.has("id")) {
                            val user = dbHelper.getUserBySocial(data.getString("id"))
                            if (user != null) {
                                saveUser(user.userId, user.name, user.email, user.profile_pic, user.socialId)
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                supportFinishAfterTransition()
                            } else {
                                data.put("picture", data.getJSONObject("picture").getJSONObject("data").getString("url"))
                                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java).apply { putExtra("socialData", data.toString()) })
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)")
                request.parameters = parameters
                request.executeAsync()

            }

            override fun onCancel() {
                //TODO Auto-generated method stub
                Toast.makeText(this@LoginActivity, "Cancel", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                //TODO Auto-generated method stub
                Toast.makeText(this@LoginActivity, "onError", Toast.LENGTH_LONG).show()
            }
        })
    }


    //GOOGLE SIGN IN-----------------------------------------------------------------------------------------------------------------------


    private fun googleSignOut() {
        // sign out Firebase
        mAuth!!.signOut()

        // sign out Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback { updateUI(null) }
    }

    private fun googleSignIn() {
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        toastLong("Please Check Your Internet Connection")
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e("GOOGLE====", "firebaseAuthWithGoogle():" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser
                        if (user != null) {
                            updateUI(user)
                        }
                    } else {
                        updateUI(null)
                    }
                }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                updateUI(null)
            }
        }
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val googleData = JSONObject()
            googleData.put("id", user.uid)
            googleData.put("email", user.email)
            googleData.put("name", user.displayName)
            googleData.put("picture", user.photoUrl)


            val user1 = dbHelper.getUserBySocial(googleData.getString("id"))
            if (user1 != null) {
                saveUser(user1.userId, user1.name, user1.email, user1.profile_pic, user1.socialId)
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                supportFinishAfterTransition()
            } else {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java).apply { putExtra("socialData", googleData.toString()) })
            }
        } else {
            //   toastShort("Signed OUT")
        }
    }
}
