package com.example.bemyhero

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView

class SetupActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var fullName: EditText
    private lateinit var countryName: EditText
    private lateinit var saveInfoButton: Button
    private lateinit var profileImage: CircleImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var userProfileImageRef: StorageReference

    private lateinit var currUserId: String
    private val galleryPick = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currUserId)
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        userName = findViewById(R.id.setup_username)
        fullName = findViewById(R.id.setup_user_fullname)
        countryName = findViewById(R.id.setup_country)
        saveInfoButton = findViewById(R.id.setup_save_button)
        profileImage = findViewById(R.id.setup_profile_image)

        saveInfoButton.setOnClickListener {
            SaveAccountSetupInfo()
        }

        profileImage.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent,galleryPick)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Bug when pressing back button. Fix it!!
        if(requestCode == galleryPick && resultCode == Activity.RESULT_OK && data != null){
            val imageUri: Uri? = data.data
            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)

            val progressBar: AlertDialog = ProgressBar()
            progressBar.show()

            if(resultCode == Activity.RESULT_OK){
                val resultUri: Uri = result.uri
                val filePath: StorageReference = userProfileImageRef.child(currUserId+".jpg")

                filePath.putFile(resultUri).addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this@SetupActivity,"Profile image stored successfully to Firebase Storage!",Toast.LENGTH_SHORT).show()
                        val downloadUrl: String = task.result?.storage?.downloadUrl.toString()

                        userRef.child("profileimage").setValue(downloadUrl)
                            .addOnCompleteListener(this) { task ->
                                if(task.isSuccessful){
                                    val selfIntent = Intent(this@SetupActivity,SetupActivity::class.java)
                                    startActivity(selfIntent)

                                    Toast.makeText(this@SetupActivity,"Profile image stored successfully to Firebase Database!",Toast.LENGTH_SHORT).show()
                                    progressBar.dismiss()
                                }
                                else{
                                    val message: String? = task.exception?.message
                                    Toast.makeText(this@SetupActivity,"Error Ocurred: $message",Toast.LENGTH_SHORT).show()
                                    progressBar.dismiss()
                                }
                            }
                    }
                }
            }
            else {
                Toast.makeText(this@SetupActivity,"Error ocurred: Image can't be cropped.\nTry again!",Toast.LENGTH_SHORT).show()
                progressBar.dismiss()
            }
        }
    }

    private fun SaveAccountSetupInfo(){
        val username: String = userName.text.toString()
        val userFullName: String = fullName.text.toString()
        val userCountry: String = countryName.text.toString()

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this@SetupActivity,"Please enter your username!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(userFullName)){
            Toast.makeText(this@SetupActivity,"Please enter your full name!",Toast.LENGTH_SHORT).show()
        }
        else if(TextUtils.isEmpty(userCountry)){
            Toast.makeText(this@SetupActivity,"Please write your country!",Toast.LENGTH_SHORT).show()
        }
        else {
            val progressBar: AlertDialog = ProgressBar()
            progressBar.show()

            val userMap = HashMap<String,String>()
            userMap.put("username",username)
            userMap.put("fullname",userFullName)
            userMap.put("country",userCountry)
            userMap.put("status","Active")
            userMap.put("gender","Unknown")
            userMap.put("dateofbirth","Unknown")
            userMap.put("relationshipstatus","Unknown")

            userRef.updateChildren(userMap as Map<String, Any>).addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    SendUserToMainActivity()
                    Toast.makeText(this@SetupActivity,"Your account was created successfully!",Toast.LENGTH_LONG).show()
                    progressBar.dismiss()
                }
                else{
                    val message: String? = task.exception?.message
                    Toast.makeText(this@SetupActivity,"Error Occurred: $message",Toast.LENGTH_SHORT).show()
                    progressBar.dismiss()
                }
            }

        }
    }

    private fun ProgressBar(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SetupActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        return builder.create()
    }

    private fun SendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@SetupActivity,MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}
