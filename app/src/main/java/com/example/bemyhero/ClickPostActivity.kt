package com.example.bemyhero

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ClickPostActivity : AppCompatActivity() {

    private lateinit var clickPostImage: ImageView
    private lateinit var clickPostDescription: TextView
    private lateinit var clickEditButton: Button
    private lateinit var clickDeleteButton: Button

    private lateinit var postKey: String
    private lateinit var currUserId: String
    private lateinit var description: String
    private lateinit var postImage: String
    private lateinit var uid: String

    private lateinit var clickRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_post)

        mAuth = FirebaseAuth.getInstance()

        currUserId = mAuth.currentUser?.uid.toString()

        postKey = intent.extras?.get("PostKey").toString()
        clickRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postKey)

        clickPostImage = findViewById(R.id.click_post_image)
        clickPostDescription = findViewById(R.id.click_post_description)
        clickEditButton = findViewById(R.id.click_edit_button)
        clickDeleteButton = findViewById(R.id.click_delete_button)

        clickEditButton.visibility = View.INVISIBLE
        clickDeleteButton.visibility = View.INVISIBLE


        clickRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    description = dataSnapshot.child("description").value.toString()
                    postImage = dataSnapshot.child("image").value.toString()

                    uid = dataSnapshot.child("uid").value.toString()

                    clickPostDescription.setText(description)

                    if(isValidGlideContext()) {
                        Glide.with(this@ClickPostActivity)
                            .load(postImage)
                            .into(clickPostImage)
                    }

                    if(currUserId.equals(uid)){
                        clickEditButton.visibility = View.VISIBLE
                        clickDeleteButton.visibility = View.VISIBLE
                    }

                    clickEditButton.setOnClickListener {
                        editPost(description)
                    }
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        clickDeleteButton.setOnClickListener {
            deletePost()
        }
    }

    fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

    private fun editPost(postDescription: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ClickPostActivity)
        builder.setTitle("Edit Post")

        val inputBox: EditText = EditText(this@ClickPostActivity)
        inputBox.setText(postDescription)
        builder.setView(inputBox)

        builder.setPositiveButton("Update") { dialog, which ->
            clickRef.child("description").setValue(inputBox.text.toString())
            Toast.makeText(this@ClickPostActivity,"Post updated successfully!",Toast.LENGTH_SHORT).show()
            sendUserToMainActivity()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        val dialog: Dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

    }

    private fun deletePost(){
        clickRef.removeValue()
        sendUserToMainActivity()
        Toast.makeText(this,"Post has been deleted successfully!",Toast.LENGTH_SHORT).show()
    }

    private fun sendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@ClickPostActivity,MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}
