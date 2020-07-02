package com.example.bemyhero

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
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

    private lateinit var displayFriends: Button
    private lateinit var displayPosts: Button
    private lateinit var friendsRef: DatabaseReference
    private var numFriends: Int = 0

    private lateinit var postsRef: DatabaseReference
    private var numPosts: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeFirebaseFields()
        initializeActivityFields()

        displayFriends.setOnClickListener {
            sendUserToFriendsActivity()
        }

        displayPosts.setOnClickListener {
            sendUserToUserPostsActivity()
        }

        friendsRef.child(currUserId).addValueEventListener(object: ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    numFriends = dataSnapshot.childrenCount.toInt()
                    displayFriends.text = "$numFriends Friends"
                }
                else {
                    displayFriends.text = "0 Friends"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        postsRef.orderByChild("uid")
            .startAt(currUserId).endAt(currUserId + "\uf8ff")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        numPosts = dataSnapshot.childrenCount.toInt()
                        displayPosts.text = "$numPosts Posts"
                    }
                    else {
                        displayPosts.text = "0 Posts"
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

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

    private fun initializeFirebaseFields(){
        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        profileRef = FirebaseDatabase.getInstance().reference.child("Users").child(currUserId)
        friendsRef = FirebaseDatabase.getInstance().reference.child("Friends")
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
    }

    private fun initializeActivityFields(){
        profileBirthday = findViewById(R.id.profile_birthday)
        profileCountry = findViewById(R.id.profile_country)
        profileGender = findViewById(R.id.profile_gender)
        profileImage = findViewById(R.id.profile_image)
        profileName = findViewById(R.id.profile_full_name)
        profileRelationship = findViewById(R.id.profile_relationship)
        profileStatus = findViewById(R.id.profile_status)
        profileUsername = findViewById(R.id.profile_user_name)

        displayFriends = findViewById(R.id.profile_friends)
        displayPosts = findViewById(R.id.profile_posts)
    }

    private fun sendUserToFriendsActivity(){
        val friendsIntent = Intent(this@ProfileActivity,FriendsActivity::class.java)
        startActivity(friendsIntent)
    }

    private fun sendUserToUserPostsActivity(){
        val userPostIntent = Intent(this@ProfileActivity,UserPostsActivity::class.java)
        startActivity(userPostIntent)
    }
}
