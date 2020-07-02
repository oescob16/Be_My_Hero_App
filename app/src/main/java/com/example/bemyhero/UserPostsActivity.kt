package com.example.bemyhero

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class UserPostsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var postList: RecyclerView
    private lateinit var postsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currUserId: String

    private var likeChecker: Boolean = false
    private lateinit var likesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_posts)

        postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        likesRef = FirebaseDatabase.getInstance().reference.child("Likes")
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()


        mToolbar = findViewById(R.id.my_posts_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "My Posts"

        postList = findViewById(R.id.my_posts_list)
        postList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        postList.layoutManager = linearLayoutManager

        displayAllMyPosts()
    }

    private fun displayAllMyPosts() {

        val sortPostsDescendingOrder: Query = postsRef.orderByChild("uid")
            .startAt(currUserId).endAt(currUserId + "\uf8ff")

        val options: FirebaseRecyclerOptions<Posts> = FirebaseRecyclerOptions.Builder<Posts>()
            .setQuery(sortPostsDescendingOrder, Posts::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options){
            override fun onBindViewHolder(viewHolder: MyPostsViewHolder, position: Int, model: Posts) {
                val postKey: String? = getRef(position).key

                viewHolder.setPost(model)

                viewHolder.mView.setOnClickListener {
                    val clickIntent: Intent =  Intent(this@UserPostsActivity,ClickPostActivity::class.java)
                    clickIntent.putExtra("PostKey",postKey)
                    startActivity(clickIntent)
                }

                if (postKey != null) {
                    viewHolder.setLikeStatus(postKey)
                }

                viewHolder.commentButton.setOnClickListener {
                    val commentsIntent: Intent =  Intent(this@UserPostsActivity,CommentsActivity::class.java)
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
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.posts_layout, parent, false)
                return MyPostsViewHolder(view)
            }
        }
        postList.adapter = firebaseRecyclerAdapter
    }

    class MyPostsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView: View = itemView

        private val username: TextView = mView.findViewById(R.id.post_username)
        private val profileImage: CircleImageView = mView.findViewById(R.id.post_profile_image)
        private val dateAndTime: TextView = mView.findViewById(R.id.post_date_time)
        private val description: TextView = mView.findViewById(R.id.post_description)
        private val postImage: ImageView = mView.findViewById(R.id.post_image)

        val likeButton: ImageButton = mView.findViewById(R.id.like_button)
        val commentButton: ImageButton = mView.findViewById(R.id.comment_button)
        val displayLikes: TextView = mView.findViewById(R.id.likes_counter)
        var likesCounter = 0
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
                @SuppressLint("SetTextI18n")
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

        private fun readableDate(date: String): String {
            val day = date.substring(0,10)
            val time = date.substring(11,16)
            return "  —  " + day.replace("-","/") + " at " + time
        }
    }
}
