package com.example.bemyhero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import de.hdodenhof.circleimageview.CircleImageView

class SetupActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var fullName: EditText
    private lateinit var countryName: EditText
    private lateinit var saveInfoButton: Button
    private lateinit var profileImage: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        userName = findViewById(R.id.setup_username)
        fullName = findViewById(R.id.setup_user_fullname)
        countryName = findViewById(R.id.setup_country)
        saveInfoButton = findViewById(R.id.setup_save_button)
        profileImage = findViewById(R.id.setup_profile_image)
    }
}
