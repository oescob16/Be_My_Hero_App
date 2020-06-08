package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar

class PostActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var selectPostImage: ImageButton
    private lateinit var updatePostButton: Button
    private lateinit var postDescription: EditText

    private val galleryPick = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

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
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent,galleryPick)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home){
            sendUserToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendUserToMainActivity(){
        val mainIntent: Intent = Intent(this@PostActivity,MainActivity::class.java)
        startActivity(mainIntent)
    }

}
