package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

class PostActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        mToolbar = findViewById(R.id.update_post_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle("Update Post")
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
