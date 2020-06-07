package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var postList: RecyclerView
    private lateinit var mToolbar: Toolbar

    private lateinit var navProfileImage: CircleImageView
    private lateinit var navProfileUsername: TextView

    private lateinit var currUserId: String

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().getReference().child("Users")

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
        navProfileImage = navView.findViewById(R.id.nav_profile_image)
        navProfileUsername = navView.findViewById(R.id.nav_user_full_name)

        userRef.child(currUserId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname")) {
                        val fullName: String = dataSnapshot.child("fullname").value.toString()
                        navProfileUsername.setText(fullName)
                    } else {
                        Toast.makeText(this@MainActivity, "Profile name doesn't exist!", Toast.LENGTH_SHORT).show()
                    }
                    if(dataSnapshot.hasChild("profileimage")){
                        val image: String = dataSnapshot.child("profileimage").value.toString()
                        Glide.with(this@MainActivity)
                            .load(image)
                            .placeholder(R.drawable.profile)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(navProfileImage)
                    } else {
                        Toast.makeText(this@MainActivity, "Profile image doesn't exist!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            override fun onCancelled(database: DatabaseError) {}
        })

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
        else{
            CheckUserExistence()
        }
    }

    private fun CheckUserExistence() {

        val currUserId: String? = mAuth.currentUser?.uid

        val userListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!dataSnapshot.hasChild(currUserId.toString())){
                    SendUserToSetupActivity()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        userRef.addValueEventListener(userListener)

    }

    private fun SendUserToSetupActivity() {
        val setupIntent: Intent = Intent(this@MainActivity,SetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(setupIntent)
        finish()
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
