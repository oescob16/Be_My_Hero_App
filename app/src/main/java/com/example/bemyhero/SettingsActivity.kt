package com.example.bemyhero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var userName: EditText
    private lateinit var userFullName: EditText
    private lateinit var userStatus: EditText
    private lateinit var userCountry: EditText
    private lateinit var userDateOfBirth: EditText
    private lateinit var userGender: EditText
    private lateinit var userRelationshipStatus: EditText
    private lateinit var userProfileImage: CircleImageView
    private lateinit var updateButton: Button

    private lateinit var settingsRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var currUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        settingsRef = FirebaseDatabase.getInstance().reference.child("Users").child(currUserId)

        mToolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userName = findViewById(R.id.settings_username)
        userFullName = findViewById(R.id.settings_fullname)
        userStatus = findViewById(R.id.settings_status)
        userCountry = findViewById(R.id.settings_country)
        userDateOfBirth = findViewById(R.id.settings_birthday)
        userGender = findViewById(R.id.settings_gender)
        userRelationshipStatus = findViewById(R.id.settings_relationship)
        userProfileImage = findViewById(R.id.settings_profile_image)
        updateButton = findViewById(R.id.settings_update_button)

        settingsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profileImageData = dataSnapshot.child("profileimage").value.toString()
                    val userNameData = dataSnapshot.child("username").value.toString()
                    val birthDayData = dataSnapshot.child("dateofbirth").value.toString()
                    val statusData = dataSnapshot.child("status").value.toString()
                    val relationshipData = dataSnapshot.child("relationshipstatus").value.toString()
                    val countryData = dataSnapshot.child("country").value.toString()
                    val fullNameData = dataSnapshot.child("fullname").value.toString()
                    val genderData = dataSnapshot.child("gender").value.toString()

                    Glide.with(this@SettingsActivity)
                        .load(profileImageData)
                        .placeholder(R.drawable.profile)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(userProfileImage)

                    userName.setText(userNameData)
                    userDateOfBirth.setText(birthDayData)
                    userStatus.setText(statusData)
                    userRelationshipStatus.setText(relationshipData)
                    userCountry.setText(countryData)
                    userFullName.setText(fullNameData)
                    userGender.setText(genderData)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}

        })

    }
}
