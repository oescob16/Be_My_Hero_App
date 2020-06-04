package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.net.Authenticator

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var postList: RecyclerView
    private lateinit var mToolbar: Toolbar
    
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Home"

        drawerLayout = findViewById(R.id.drawable_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.drawer_open,R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.navigation_view)
        var navView: View = navigationView.inflateHeaderView(R.layout.navigation_header)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            userMenuSelector(menuItem)
            false
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? =  mAuth.currentUser
        if(currentUser == null){
            SendUserToLoginActivity()
        }
    }

    private fun SendUserToLoginActivity(){
        val loginIntent: Intent = Intent(this@MainActivity,LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun userMenuSelector(item: MenuItem){
        when(item.itemId){
            R.id.nav_profile -> {
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_home -> {
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_friends -> {
                Toast.makeText(this,"Friends List",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_find_friends -> {
                Toast.makeText(this,"Find Friends",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_messages -> {
                Toast.makeText(this,"Messages",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                mAuth.signOut()
                SendUserToLoginActivity()
            }
        }
    }
}
