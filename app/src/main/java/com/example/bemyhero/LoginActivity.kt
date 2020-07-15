package com.example.bemyhero

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var googleButton: ImageView

    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var newAccountLink: TextView
    private lateinit var forgottenPasswordLink: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var currUserId: String

    private lateinit var progressBar: AlertDialog

    private val RC_SIGN_IN: Int = 1
    private lateinit var googleSignInClient: GoogleSignInClient
    private var TAG: String = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        progressBar = ProgressBar()

        newAccountLink = findViewById(R.id.register_account_link)
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)

        forgottenPasswordLink = findViewById(R.id.forgot_password_link)

        forgottenPasswordLink.setOnClickListener {
            sendUserToResetPasswordActivity()
        }

        googleButton = findViewById(R.id.google_button)

        newAccountLink.setOnClickListener {
            SendUserToRegisterActivity()
        }

        loginButton.setOnClickListener {
            allowUserToLogin()
        }

        createGoogleRequest()

        googleButton.setOnClickListener {
            googleSignIn()
        }
    }

    private fun createGoogleRequest(){

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            progressBar.setTitle("Google Sign In")
            progressBar.show()

            val result: GoogleSignInResult? = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                // Google Sign In was successful, authenticate with Firebase
                if(result.isSuccess){
                    val account: GoogleSignInAccount? = result.signInAccount
                    if (account != null) {
                        firebaseAuthWithGoogle(account)
                    }
                    Toast.makeText(this@LoginActivity,"Please wait, while we are getting your information!",Toast.LENGTH_SHORT).show()
                }
                // Google Sign In failed
                else {
                    Toast.makeText(this@LoginActivity,"Sorry, but we are not able to get your information!",Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(acct.idToken,null)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                // Sign in success
                if (task.isSuccessful) {
                    updateOnlineStatus("online")
                    sendUserToMainActivity()
                    progressBar.dismiss()
                }
                // If sign in fails, display a message to the user.
                else {
                    val message: String = task.exception.toString()
                    sendUserToLoginActivity()
                    Toast.makeText(this@LoginActivity,"Error occurred: $message.\nPlease, try again!",Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? =  mAuth.currentUser
        if(currentUser != null){
            sendUserToMainActivity()
        }
    }

    private fun allowUserToLogin(){

        val email: String = userEmail.text.toString()
        val password: String = userPassword.text.toString()

        progressBar.setTitle("Manual Sign In")
        progressBar.show()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this@LoginActivity,"Please enter your email!",Toast.LENGTH_SHORT).show()
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this@LoginActivity,"Please enter your password!",Toast.LENGTH_SHORT).show()
        }
        else {
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        updateOnlineStatus("online")
                        sendUserToMainActivity()
                        Toast.makeText(this@LoginActivity,"You have successfully logged in!",Toast.LENGTH_SHORT).show()
                        progressBar.dismiss()
                    }
                    else {
                        val message: String? = task.exception?.message
                        Toast.makeText(this@LoginActivity,"Error Occurred: $message",Toast.LENGTH_SHORT).show()
                        progressBar.dismiss()
                    }
                }
        }
    }

    private fun updateOnlineStatus(status: String) {

        currUserId = mAuth.currentUser?.uid.toString()

        val currDate: String = getDate()
        val currTime: String = getTime()

        val currStateMap = HashMap<String, String>()
        currStateMap["date"] = currDate
        currStateMap["time"] = currTime
        currStateMap["type"] = status

        if(currUserId != "null"){
            userRef.child(currUserId).child("OnlineStatus")
                .updateChildren(currStateMap as Map<String, Any>)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String{
        val calendar: Calendar = Calendar.getInstance()
        val currDate = SimpleDateFormat("dd/MM/yyyy")
        return currDate.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currTime = SimpleDateFormat("hh:mm a")
        return currTime.format(calendar.time)
    }

    private fun sendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@LoginActivity,MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun SendUserToRegisterActivity(){
        val registerIntent: Intent = Intent(this@LoginActivity,RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun sendUserToResetPasswordActivity(){
        val resetPasswordIntent: Intent = Intent(this@LoginActivity,ResetPasswordActivity::class.java)
        startActivity(resetPasswordIntent)
    }

    private fun sendUserToLoginActivity(){
        val loginIntent: Intent = Intent(this@LoginActivity,LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    private fun ProgressBar(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@LoginActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.create()
    }
}
