package com.example.bemyhero

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.posts_layout.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var postList: RecyclerView
    private lateinit var mToolbar: Toolbar

    private lateinit var navProfileImage: CircleImageView
    private lateinit var navProfileUsername: TextView
    private lateinit var addNewPost: ImageButton

    private lateinit var currUserId: String
    private var likeChecker: Boolean = false

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var postsRef: DatabaseReference
    private lateinit var likesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        likesRef = FirebaseDatabase.getInstance().reference.child("Likes")

        mToolbar = findViewById(R.id.main_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Home"

        addNewPost = findViewById(R.id.add_new_post_button)

        drawerLayout = findViewById(R.id.drawable_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.drawer_open,R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.navigation_view)

        postList = findViewById(R.id.all_users_post_list)
        postList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList.layoutManager = linearLayoutManager

        val navView: View = navigationView.inflateHeaderView(R.layout.navigation_header)
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

                        if(isValidGlideContext()){
                            Glide.with(this@MainActivity)
                                .load(image)
                                .placeholder(R.drawable.profile)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(navProfileImage)
                        }
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

        addNewPost.setOnClickListener {
            sendUserToPostActivity()
        }

        displayUsersPosts()
    }

    fun Context.isValidGlideContext() = this !is Activity || (!this.isDestroyed && !this.isFinishing)

    private fun displayUsersPosts() {

        val sortPostsDescendingOrder: Query = postsRef.orderByChild("positionofpost")

        val options: FirebaseRecyclerOptions<Posts> = FirebaseRecyclerOptions.Builder<Posts>()
            .setQuery(sortPostsDescendingOrder, Posts::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            override fun onBindViewHolder(viewHolder: PostsViewHolder, position: Int, model: Posts) {
                val postKey: String? = getRef(position).key

                viewHolder.setPost(model)

                viewHolder.mView.setOnClickListener {
                    val clickIntent: Intent =  Intent(this@MainActivity,ClickPostActivity::class.java)
                    clickIntent.putExtra("PostKey",postKey)
                    startActivity(clickIntent)
                }

                if (postKey != null) {
                    viewHolder.setLikeStatus(postKey)
                }

                viewHolder.commentButton.setOnClickListener {
                    val commentsIntent: Intent =  Intent(this@MainActivity,CommentsActivity::class.java)
                    commentsIntent.putExtra("PostKey",postKey)
                    startActivity(commentsIntent)
                }

                viewHolder.likeButton.setOnClickListener {
                    likeChecker = true

                    likesRef.addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(likeChecker){
                                likeChecker = if (postKey?.let { it -> dataSnapshot.child(it).hasChild(currUserId) }!!){
                                    likesRef.child(postKey).child(currUserId).removeValue()
                                    false
                                } else {
                                    likesRef.child(postKey).child(currUserId).setValue(true)
                                    false
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.posts_layout, parent, false)
                return PostsViewHolder(view)
            }
        }
        postList.adapter = firebaseRecyclerAdapter
    }

    class PostsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView: View = itemView

        private val username: TextView = mView.findViewById(R.id.post_username)
        private val profileImage: CircleImageView = mView.findViewById(R.id.post_profile_image)
        private val dateAndTime: TextView = mView.findViewById(R.id.post_date_time)
        private val description: TextView = mView.findViewById(R.id.post_description)
        private val postImage: ImageView = mView.findViewById(R.id.post_image)

        val likeButton: ImageButton = mView.findViewById(R.id.like_button)
        val commentButton: ImageButton = mView.findViewById(R.id.comment_button)
        val displayLikes: TextView = mView.findViewById(R.id.likes_counter)
        var likesCounter: Int = 0
        private val currUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
        private val likesRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Likes")

        fun setPost(posts: Posts){
            username.text = posts.fullname
            dateAndTime.text = readableDate(posts.dateAndTime.toString())
            description.text = posts.description
            Glide.with(itemView.context)
                .load(posts.image)
                .into(postImage)
            Glide.with(itemView.context)
                .load(posts.profileimage)
                .into(profileImage)
        }

        fun setLikeStatus(postKey: String){

            likesRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(currUserId?.let { dataSnapshot.child(postKey).hasChild(it) }!!){
                        likesCounter = dataSnapshot.child(postKey).childrenCount.toInt()
                        likeButton.setImageResource(R.drawable.like)
                        displayLikes.text = "$likesCounter Likes"
                    }
                    else {
                        likesCounter = dataSnapshot.child(postKey).childrenCount.toInt()
                        likeButton.setImageResource(R.drawable.unlike)
                        displayLikes.text = "$likesCounter Likes"
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }

        fun readableDate(date: String): String {
            val day = date.substring(0,10)
            val time = date.substring(11,16)
            return "  —  " + day.replace("-","/") + " at " + time
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

    private fun sendUserToPostActivity(){
        val postIntent: Intent = Intent(this@MainActivity,PostActivity::class.java)
        startActivity(postIntent)
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

    private fun sendUserToSettingsActivity(){
        val settingsIntent: Intent = Intent(this@MainActivity,SettingsActivity::class.java)
        startActivity(settingsIntent)
    }

    private fun sendUserToFindFriendsActivity(){
        val findFriendsIntent: Intent = Intent(this@MainActivity,FindFriendsActivity::class.java)
        startActivity(findFriendsIntent)
    }

    private fun sendUserToProfileActivity(){
        val profileIntent: Intent = Intent(this@MainActivity,ProfileActivity::class.java)
        startActivity(profileIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun userMenuSelector(item: MenuItem){
        when(item.itemId){
            R.id.nav_post -> {
                sendUserToPostActivity()
            }
            R.id.nav_profile -> {
                sendUserToProfileActivity()
            }
            R.id.nav_home -> {
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_friends -> {
                Toast.makeText(this,"Friends List",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_find_friends -> {
                sendUserToFindFriendsActivity()
            }
            R.id.nav_messages -> {
                Toast.makeText(this,"Messages",Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                sendUserToSettingsActivity()
            }
            R.id.nav_logout -> {
                mAuth.signOut()
                SendUserToLoginActivity()
            }
        }
    }
}
