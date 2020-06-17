package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CommentsActivity : AppCompatActivity() {

    private lateinit var postCommentButton: ImageButton
    private lateinit var userCommentInput: EditText
    private lateinit var commentSection: RecyclerView

    private lateinit var postKey: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currUserId: String
    private lateinit var userRef: DatabaseReference
    private lateinit var postsRef: DatabaseReference

    private lateinit var saveCurrDateAndTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        postKey = intent.extras?.get("PostKey").toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currUserId)
        postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postKey).child("Comments")

        postCommentButton = findViewById(R.id.post_comment_button)
        userCommentInput = findViewById(R.id.comment_input)

        commentSection = findViewById(R.id.comments_list)
        commentSection.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        commentSection.layoutManager = linearLayoutManager

        postCommentButton.setOnClickListener {
            userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        val userFullName: String = dataSnapshot.child("fullname").value.toString()
                        val userProfileImage: String = dataSnapshot.child("profileimage").value.toString()
                        postComment(userFullName, userProfileImage)

                        userCommentInput.setText("")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    override fun onStart() {
        super.onStart()

        val options: FirebaseRecyclerOptions<Comments> = FirebaseRecyclerOptions.Builder<Comments>()
            .setQuery(postsRef, Comments::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = object: FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            override fun onBindViewHolder(viewHolder: CommentsViewHolder, position: Int, model: Comments) {
                viewHolder.setCommentsInfo(model)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comments_layout, parent, false)
                return CommentsViewHolder(view)
            }
        }
        adapter.startListening()
        commentSection.adapter = adapter
    }

    class CommentsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView: View = itemView

        val commentsFullName: TextView = mView.findViewById(R.id.comment_user_full_name)
        val commentsText: TextView = mView.findViewById(R.id.comment_text)
        val commentsDate: TextView = mView.findViewById(R.id.comment_date)
        val commentsTime: TextView = mView.findViewById(R.id.comment_time)
        val commentsProfileImage: CircleImageView = mView.findViewById(R.id.comment_profile_image)

        fun setCommentsInfo(comments: Comments){
            commentsFullName.text = comments.fullname
            commentsText.text = comments.comment
            commentsDate.text = comments.dateAndTime?.substring(0,10)
            commentsTime.text = comments.dateAndTime?.substring(11,16)
            Glide.with(itemView.context)
                .load(comments.profileimage)
                .into(commentsProfileImage)
        }
    }

    fun postComment(userFullName: String, userProfileImage: String){
        val commentText: String = userCommentInput.text.toString()
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this,"Please write a comment!",Toast.LENGTH_SHORT).show()
        }
        else {
            val currDate = getDate()
            val randomCommentName: String = currUserId + currDate

            val commentsMap = HashMap<String,String>()
            commentsMap["uid"] = currUserId
            commentsMap["comment"] = commentText
            commentsMap["dateAndTime"] = currDate
            commentsMap["fullname"] = userFullName
            commentsMap["profileimage"] = userProfileImage

            postsRef.child(randomCommentName).updateChildren(commentsMap as Map<String, Any>).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this,"You have commented this post succesfully",Toast.LENGTH_SHORT).show()
                }
                else {
                    val message: String = task.exception?.message.toString()
                    Toast.makeText(this,"Error occurred: $message\nPlease try again!",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun getDate(): String{
        val calendar: Calendar = Calendar.getInstance()
        val currDateAndTime = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss")
        saveCurrDateAndTime = currDateAndTime.format(calendar.time)
        return saveCurrDateAndTime
    }

}
