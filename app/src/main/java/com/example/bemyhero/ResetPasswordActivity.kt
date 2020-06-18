package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    private lateinit var sendEmailButton: Button
    private lateinit var userEmailInput: EditText

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        mAuth = FirebaseAuth.getInstance()

        mToolbar = findViewById(R.id.forget_password_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Reset Password"

        sendEmailButton = findViewById(R.id.send_email_button)
        userEmailInput = findViewById(R.id.user_email_input)

        sendEmailButton.setOnClickListener {
            val userEmail: String = userEmailInput.text.toString().trim()

            if(TextUtils.isEmpty(userEmail)){
                Toast.makeText(this@ResetPasswordActivity,"Please write your email address!",Toast.LENGTH_SHORT).show()
            }
            else {
                mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this@ResetPasswordActivity,"We have sent you an email to reset your password!",Toast.LENGTH_SHORT).show()
                        sendUserToLoginActivity()
                    }
                    else {
                        val message: String = task.exception.toString()
                        Toast.makeText(this@ResetPasswordActivity,"Error occurred: $message.\nPlease try again!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    fun sendUserToLoginActivity(){
        val loginIntent = Intent(this@ResetPasswordActivity,LoginActivity::class.java)
        startActivity(loginIntent)
    }
}
