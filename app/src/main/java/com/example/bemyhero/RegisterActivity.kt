package com.example.bemyhero

import android.app.AlertDialog
import android.app.IntentService
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var userConfirmPassword: EditText
    private lateinit var createAccountButton: Button
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        userEmail = findViewById(R.id.register_email)
        userPassword = findViewById(R.id.register_password)
        userConfirmPassword = findViewById(R.id.register_confirm_password)
        createAccountButton = findViewById(R.id.register_create_account)


        createAccountButton.setOnClickListener {
            CreateNewAccount()
        }
    }

    private fun CreateNewAccount(){
        val email: String = userEmail.text.toString()
        val password: String = userPassword.text.toString()
        val confirmPassword: String = userConfirmPassword.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email!",Toast.LENGTH_SHORT).show()
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password!",Toast.LENGTH_SHORT).show()
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this,"Please confirm your password!",Toast.LENGTH_SHORT).show()
        }
        else if (!password.equals(confirmPassword)){
            Toast.makeText(this,"Your Password and Confirmation Password don't match.\nPlease try again!",Toast.LENGTH_SHORT).show()
        }
        else {

            val builder: AlertDialog.Builder = AlertDialog.Builder(this@RegisterActivity)
            builder.setCancelable(false)
            builder.setView(R.layout.loading_dialog)
            val dialog: AlertDialog = builder.create()
            dialog.show()

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){
                        SendUserToSetupActivity()
                        Toast.makeText(this@RegisterActivity,"Your authentication was successful!",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        }
                    else{
                        val message: String? = task.exception?.message
                        Toast.makeText(this@RegisterActivity,"Error Occured: $message",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        }
                }
        }
    }

    private fun SendUserToSetupActivity(){
        val setupIntent: Intent = Intent(this@RegisterActivity, SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()

    }
}
