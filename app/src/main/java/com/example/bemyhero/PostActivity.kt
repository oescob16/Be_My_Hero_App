package com.example.bemyhero

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PostActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var selectPostImage: ImageButton
    private lateinit var updatePostButton: Button
    private lateinit var postDescription: EditText

    private val galleryPick = 1
    private var imageUri: Uri? = null
    private lateinit var description: String

    private lateinit var userPostImageRef: StorageReference
    private lateinit var userRef: DatabaseReference
    private lateinit var postRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var saveCurrDateAndTime: String
    private lateinit var postRandomName: String
    private lateinit var currUserId: String

    private lateinit var progressBar: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()

        postRandomName = getDate()
        progressBar = ProgressBar()

        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        userPostImageRef = FirebaseStorage.getInstance().reference

        userRef.keepSynced(true)

        selectPostImage = findViewById(R.id.select_post_image)
        updatePostButton = findViewById(R.id.update_post_button)
        postDescription = findViewById(R.id.post_description)

        mToolbar = findViewById(R.id.update_post_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle("Update Post")

        selectPostImage.setOnClickListener {
            openGallery()
        }

        updatePostButton.setOnClickListener {
            updatePostInfo()
        }
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent,galleryPick)
    }

    private fun updatePostInfo(){
        description = postDescription.text.toString()

        if(imageUri == null){
            Toast.makeText(this@PostActivity,"Please select an image!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this@PostActivity,"Please provide details about your post!",Toast.LENGTH_SHORT).show()
        }
        else{
            storeImageToFirebaseStorage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == galleryPick && resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.data!!
            selectPostImage.setImageURI(imageUri)
        }
    }

    private fun storeImageToFirebaseStorage() {
        progressBar.show()

        val filePath: StorageReference = userPostImageRef.child("Post Images")
            .child(imageUri?.lastPathSegment + postRandomName + ".jpg")

        imageUri?.let {
            filePath.putFile(it).addOnSuccessListener(this) {
                filePath.downloadUrl.addOnSuccessListener(this) { uri: Uri ->

                    val downloadUrl = uri.toString()

                    postRef.child(currUserId + postRandomName).child("image").setValue(downloadUrl).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Toast.makeText(this@PostActivity,"Profile image stored successfully to Firebase Storage!",Toast.LENGTH_SHORT).show()
                            savingPostInfoToDatabase()
                        }
                        else {
                            val message: String? = task.exception?.message
                            Toast.makeText(this@PostActivity,"Error occurred: $message",Toast.LENGTH_SHORT).show()
                            progressBar.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun savingPostInfoToDatabase(){

        userRef.child(currUserId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val userFullName: String = dataSnapshot.child("fullname").getValue().toString()
                    val userProfileImage: String = dataSnapshot.child("profileimage").getValue().toString()

                    val postMap = HashMap<String,String>()
                    postMap["uid"] = currUserId
                    postMap["dateAndTime"] = saveCurrDateAndTime
                    postMap["description"] = description
                    postMap["profileimage"] = userProfileImage
                    postMap["fullname"] = userFullName
                    postMap["positionofpost"] = getPositionOfPost()

                    postRef.child(currUserId + postRandomName).updateChildren(postMap as Map<String, Any>).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            sendUserToMainActivity()
                            Toast.makeText(this@PostActivity,"New post stored successfully to Firebase Database!",Toast.LENGTH_LONG).show()
                            progressBar.dismiss()
                        }
                        else {
                            Toast.makeText(this@PostActivity,"Error occurred while storing your post!",Toast.LENGTH_LONG).show()
                            progressBar.dismiss()
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getPositionOfPost(): String {
        val calendar: Calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS")
        val dateOfPost = date.format(calendar.time)
        return dateOfPost.replace(Regex("[-:.]"),"")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home){
            sendUserToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendUserToMainActivity(){
        val mainIntent = Intent(this@PostActivity,MainActivity::class.java)
        startActivity(mainIntent)
    }

    private fun ProgressBar(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@PostActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.create()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(): String {
        val calendar: Calendar = Calendar.getInstance()
        val currDateAndTime = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss")
        saveCurrDateAndTime = currDateAndTime.format(calendar.time)
        return saveCurrDateAndTime
    }

}
