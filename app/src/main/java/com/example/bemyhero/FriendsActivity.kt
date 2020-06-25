package com.example.bemyhero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class FriendsActivity : AppCompatActivity() {

    private lateinit var friendsList: RecyclerView
    private lateinit var mToolbar: Toolbar
    private lateinit var friendsRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        mAuth = FirebaseAuth.getInstance()
        currUserId = mAuth.currentUser?.uid.toString()
        friendsRef = FirebaseDatabase.getInstance().reference.child("Friends").child(currUserId)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        friendsList = findViewById(R.id.friends_list)
        friendsList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        friendsList.layoutManager = linearLayoutManager

        mToolbar = findViewById(R.id.friends_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Friends"

        displayFriends()
    }

    private fun displayFriends(){
        val options: FirebaseRecyclerOptions<Friends> = FirebaseRecyclerOptions.Builder<Friends>()
            .setQuery(friendsRef, Friends::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options){
            override fun onBindViewHolder(viewHolder: FriendsViewHolder, position: Int, model: Friends) {
                val usersIDs: String? = getRef(position).key
                if (usersIDs != null) {
                    userRef.child(usersIDs).addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(dataSnapshot.exists()){
                                val userName: String = dataSnapshot.child("fullname").value.toString()
                                val userProfileImage: String = dataSnapshot.child("profileimage").value.toString()

                                viewHolder.setInfo(model, userName, userProfileImage)

                                viewHolder.mView.setOnClickListener {
                                    val options = arrayOf("$userName's profile", "Send Message")
                                    val alert: AlertDialog.Builder = AlertDialog.Builder(this@FriendsActivity)
                                    alert.setTitle("Select Option")
                                    alert.setItems(options) { dialog, which ->
                                        when(which){
                                            0 -> sendUserToFriendsProfileActivity(usersIDs)
                                            1 -> sendUserToChatActivity(usersIDs, userName)
                                        }
                                    }
                                    alert.show()
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)
                return FriendsViewHolder(view)
            }
        }
        friendsList.adapter = firebaseRecyclerAdapter
    }

    private fun sendUserToChatActivity(visitUserKey: String?, userName: String){
        val chatIntent = Intent(this@FriendsActivity,ChatActivity::class.java)
        chatIntent.putExtra("FRIEND_ID",visitUserKey)
        chatIntent.putExtra("FRIEND_FULL_NAME",userName)
        startActivity(chatIntent)
    }

    private fun sendUserToFriendsProfileActivity(visitUserKey: String?) {
        val friendProfileIntent = Intent(this@FriendsActivity,FriendsProfileActivity::class.java)
        friendProfileIntent.putExtra("FRIEND_ID",visitUserKey)
        startActivity(friendProfileIntent)
    }

    class FriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView: View = itemView

        val friendsProfileImage: CircleImageView = mView.findViewById(R.id.find_users_profile_image)
        val friendsFullName: TextView = mView.findViewById(R.id.find_user_full_name)
        val friendsDate: TextView = mView.findViewById(R.id.find_user_status)

        fun setInfo(friend: Friends, userName: String, userProfileImage: String){
            friendsFullName.text = userName
            val dateSince: String = friend.date.toString()
            friendsDate.text = "Friends since: ${dateSince.substring(0,10)}"
            Glide.with(itemView.context)
                .load(userProfileImage)
                .into(friendsProfileImage)
        }
    }
}
