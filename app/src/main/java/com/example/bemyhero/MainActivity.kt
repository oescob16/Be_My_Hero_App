package com.example.bemyhero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var postList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawable_layout)
        navigationView = findViewById(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener( NavigationView.OnNavigationItemSelectedListener { menuItem ->
            userMenuSelector(menuItem)
            false
        })
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
                Toast.makeText(this,"Logout",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
