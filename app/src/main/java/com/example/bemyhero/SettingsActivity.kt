package com.example.bemyhero

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView


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
    private lateinit var userProfileImageRef: StorageReference

    private lateinit var currUserId: String
    private var downloadUrl: String? = null
    private var hasChangedProfileImage: Boolean = false
    private var hasChangedFullName: Boolean = false

    private val galleryPick = 1
    private lateinit var progressBar: AlertDialog

    private var TAG: String = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        progressBar = ProgressBar()

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        settingsRef = FirebaseDatabase.getInstance().reference.child("Users").child(currUserId)
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

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
                    val fullNameData = dataSnapshot.child("fullname").value.toString()
                    val profileImageData = dataSnapshot.child("profileimage").value.toString()
                    val userNameData = dataSnapshot.child("username").value.toString()
                    val birthDayData = dataSnapshot.child("dateofbirth").value.toString()
                    val statusData = dataSnapshot.child("status").value.toString()
                    val relationshipData = dataSnapshot.child("relationshipstatus").value.toString()
                    val countryData = dataSnapshot.child("country").value.toString()
                    val genderData = dataSnapshot.child("gender").value.toString()

                    if(isValidGlideContext()) {
                        Glide.with(this@SettingsActivity)
                            .load(profileImageData)
                            .placeholder(R.drawable.profile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userProfileImage)
                    }

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

        updateButton.setOnClickListener {
            updateInfo()
        }

        userProfileImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent,galleryPick)
        }
    }

    fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        hasChangedProfileImage = true

        if(requestCode == galleryPick && data != null){
            val imageUri: Uri? = data.data

            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null){
            val selfIntent = Intent(this@SettingsActivity,SettingsActivity::class.java)
            selfIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // new?
            startActivity(selfIntent)

            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)

            progressBar.setTitle("Updating profile image")
            progressBar.show()

            if(resultCode == Activity.RESULT_OK){
                val resultUri: Uri = result.uri
                val filePath: StorageReference = userProfileImageRef.child(currUserId+".jpg")

                filePath.putFile(resultUri).addOnSuccessListener(this) {
                    filePath.downloadUrl.addOnSuccessListener(this) { uri: Uri ->

                        Toast.makeText(this@SettingsActivity,"Profile image stored successfully to Firebase Storage!",Toast.LENGTH_SHORT).show()
                        downloadUrl = uri.toString()

                        settingsRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Toast.makeText(this@SettingsActivity,"Profile image stored successfully to Firebase Database!",Toast.LENGTH_SHORT).show()
                                progressBar.dismiss()
                            } else {
                                val message: String? = task.exception?.message
                                Toast.makeText(this@SettingsActivity,"Error Occurred: $message",Toast.LENGTH_SHORT).show()
                                progressBar.dismiss()
                            }
                        }
                    }
                }
            }
            else {
                Toast.makeText(this@SettingsActivity,"Error occurred: Image can't be cropped.\nTry again!",Toast.LENGTH_SHORT).show()
                progressBar.dismiss()
            }
        }
        if(data == null){
            val selfIntent = Intent(this@SettingsActivity,SettingsActivity::class.java)
            selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(selfIntent)
        }
    }

    private fun updateInfo(){
        val newUsername: String = userName.text.toString()
        val newDateOfBirth: String = userDateOfBirth.text.toString()
        val newUserStatus: String = userStatus.text.toString()
        val newRelationshipStatus: String = userRelationshipStatus.text.toString()
        val newCountry: String = userCountry.text.toString()
        val newFullName: String = userFullName.text.toString()
        val newGender: String = userGender.text.toString()

        hasChangedFullName = true

        if(TextUtils.isEmpty(newUserStatus)){
            Toast.makeText(this@SettingsActivity,"Please enter your status!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newFullName)){
            Toast.makeText(this@SettingsActivity,"Please enter your full name!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newUsername)){
            Toast.makeText(this@SettingsActivity,"Please enter your username!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newCountry)){
            Toast.makeText(this@SettingsActivity,"Please enter your country!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newDateOfBirth)){
            Toast.makeText(this@SettingsActivity,"Please enter your date of birth!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newGender)){
            Toast.makeText(this@SettingsActivity,"Please enter your gender!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(newRelationshipStatus)){
            Toast.makeText(this@SettingsActivity,"Please enter your relationship status!",Toast.LENGTH_SHORT).show()
        }
        else {
            progressBar.setTitle("Saving new information")
            progressBar.show()
            updateAccount(newUserStatus,newFullName,newUsername,newCountry,newDateOfBirth,newGender,newRelationshipStatus)
        }

    }

    private fun updateAccount(status: String, fullname: String, username: String, country: String, dob: String, gender: String, relationStatus: String) {
        val userMap = HashMap<String,String>()

        userMap.put("username",username)
        userMap.put("status",status)
        userMap.put("fullname",fullname)
        userMap.put("country",country)
        userMap.put("dateofbirth",dob)
        userMap.put("gender",gender)
        userMap.put("relationshipstatus",relationStatus)

        settingsRef.updateChildren(userMap as Map<String, Any>).addOnCompleteListener(this) { task ->
            if(task.isSuccessful){
                sendUserToMainActivity()
                Toast.makeText(this@SettingsActivity,"Your account has been updated successfully!",Toast.LENGTH_SHORT).show()
                progressBar.dismiss()
            }
            else {
                val message: String = task.exception?.message.toString()
                Toast.makeText(this@SettingsActivity,"Error occurred: $message.\nPlease try again!",Toast.LENGTH_SHORT).show()
                progressBar.dismiss()
            }
        }
        // Optimize both methods in the final version to make it faster and efficient.
        updateAllPosts(downloadUrl,fullname)
        updateAllComments(downloadUrl,fullname)
    }

    private fun updateAllComments(newProfileImage: String?, newFullName: String){
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        commentsRef.addListenerForSingleValueEvent( object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (posts in dataSnapshot.children){
                    val postKey = posts.key.toString()
                    updateAllCommentsHelper(newProfileImage,newFullName,postKey, commentsRef)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message)
            }
        })
    }

    private fun updateAllCommentsHelper(newProfileImage: String?, newFullName: String, postKey: String, ref: DatabaseReference){
        val commentsRef = ref.child(postKey).child("Comments")
        val query: Query = commentsRef.orderByChild("uid").equalTo(currUserId)

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(hasChangedProfileImage) {
                    for (ds in dataSnapshot.children) {
                        ds.child("profileimage").ref.setValue(newProfileImage)
                    }
                }
                if(hasChangedFullName) {
                    for (ds in dataSnapshot.children) {
                        ds.child("fullname").ref.setValue(newFullName)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message)
            }
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun updateAllPosts(newProfileImage: String?, newFullName: String) {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        val query: Query = postsRef.orderByChild("uid").equalTo(currUserId)

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(hasChangedProfileImage) {
                    for (ds in dataSnapshot.children) {
                        ds.child("profileimage").ref.setValue(newProfileImage)
                    }
                }
                if(hasChangedFullName) {
                    for (ds in dataSnapshot.children) {
                        ds.child("fullname").ref.setValue(newFullName)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message)
            }
        }
        query.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun sendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@SettingsActivity,MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun ProgressBar(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.create()
    }
}
