package com.example.bemyhero

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class FriendsProfileActivity : AppCompatActivity() {

    private lateinit var profileName: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileStatus: TextView
    private lateinit var profileBirthday: TextView
    private lateinit var profileGender: TextView
    private lateinit var profileCountry: TextView
    private lateinit var profileRelationship: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var acceptRequestButton: Button
    private lateinit var deleteRequestButton: Button

    private lateinit var profileRef: DatabaseReference
    private lateinit var friendsProfileRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var senderUserId: String
    private lateinit var receiverUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_profile)

        initializeFirebaseFields()
        initializeActivityFields()
        displayFriendsProfile()

    }

    private fun displayFriendsProfile() {
        friendsProfileRef.addValueEventListener(object: ValueEventListener {
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

                    Glide.with(this@FriendsProfileActivity)
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

    private fun initializeFirebaseFields(){
        mAuth = FirebaseAuth.getInstance()
        receiverUserId = intent.extras?.get("FRIEND_ID").toString()
        friendsProfileRef = FirebaseDatabase.getInstance().reference.child("Users").child(receiverUserId)
    }

    private fun initializeActivityFields(){
        profileBirthday = findViewById(R.id.friend_profile_birthday)
        profileCountry = findViewById(R.id.friend_profile_country)
        profileGender = findViewById(R.id.friend_profile_gender)
        profileImage = findViewById(R.id.friend_profile_image)
        profileName = findViewById(R.id.friend_profile_full_name)
        profileRelationship = findViewById(R.id.friend_profile_relationship)
        profileStatus = findViewById(R.id.friend_profile_status)
        profileUsername = findViewById(R.id.friend_profile_user_name)
        acceptRequestButton = findViewById(R.id.accept_friend_request_button)
        deleteRequestButton = findViewById(R.id.decline_friend_request_button)
    }
}
