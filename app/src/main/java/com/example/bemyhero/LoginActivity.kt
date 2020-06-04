package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var newAccountLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        newAccountLink = findViewById(R.id.register_account_link)
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)

        newAccountLink.setOnClickListener {
            SendUserToRegisterActivity()
        }
    }

    private fun SendUserToRegisterActivity(){
        val registerIntent: Intent = Intent(this@LoginActivity,RegisterActivity::class.java)
        startActivity(registerIntent)
    }
}
