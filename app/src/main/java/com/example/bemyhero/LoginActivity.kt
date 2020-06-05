package com.example.bemyhero

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var newAccountLink: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        newAccountLink = findViewById(R.id.register_account_link)
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)

        newAccountLink.setOnClickListener {
            SendUserToRegisterActivity()
        }

        loginButton.setOnClickListener {
            AllowUserToLogin()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? =  mAuth.currentUser
        if(currentUser != null){
            SendUserToMainActivity()
        }
    }

    private fun SendUserToRegisterActivity(){
        val registerIntent: Intent = Intent(this@LoginActivity,RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    private fun AllowUserToLogin(){

        val email: String = userEmail.text.toString()
        val password: String = userPassword.text.toString()

        val progressBar: AlertDialog = ProgressBar()
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
                        SendUserToMainActivity()
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

    private fun SendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@LoginActivity,MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun ProgressBar(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@LoginActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.create()
    }
}
