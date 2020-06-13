package com.example.bemyhero

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileName: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileStatus: TextView
    private lateinit var profileBirthday: TextView
    private lateinit var profileGender: TextView
    private lateinit var profileCountry: TextView
    private lateinit var profileRelationship: TextView
    private lateinit var profileImage: CircleImageView

    private lateinit var profileRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var currUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        profileRef = FirebaseDatabase.getInstance().reference.child("Users").child(currUserId)

        profileBirthday = findViewById(R.id.profile_birthday)
        profileCountry = findViewById(R.id.profile_country)
        profileGender = findViewById(R.id.profile_gender)
        profileImage = findViewById(R.id.profile_image)
        profileName = findViewById(R.id.profile_full_name)
        profileRelationship = findViewById(R.id.profile_relationship)
        profileStatus = findViewById(R.id.profile_status)
        profileUsername = findViewById(R.id.profile_user_name)

        profileRef.addValueEventListener(object: ValueEventListener {
            @SuppressLint("SetTextI18n")
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

                    Glide.with(this@ProfileActivity)
                        .load(profileImageData)
                        .placeholder(R.drawable.profile)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage)

                    profileUsername.text = "@$userNameData"
                    profileBirthday.text = "Date of birth: $birthDayData"
                    profileStatus.text = statusData
                    profileRelationship.text = "Relationship status: $relationshipData"
                    profileCountry.text = "Country: $countryData"
                    profileName.text = fullNameData
                    profileGender.text = "Gender: $genderData"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
