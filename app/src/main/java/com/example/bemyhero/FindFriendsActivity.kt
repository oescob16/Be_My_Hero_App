package com.example.bemyhero

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseArray
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList


class FindFriendsActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar

    private lateinit var searchButton: ImageButton
    private lateinit var searchInputText: EditText

    private lateinit var searchOutputList: RecyclerView

    private lateinit var allUsersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        allUsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        mToolbar = findViewById(R.id.find_friends_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Search Friends"

        searchButton = findViewById(R.id.search_button)
        searchInputText = findViewById(R.id.search_input_box)

        searchOutputList = findViewById(R.id.search_list)
        searchOutputList.setHasFixedSize(true)
        searchOutputList.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener{
            searchFriends(searchInputText.text.toString())
        }
    }

    private fun searchFriends(userInputText: String) {
        Toast.makeText(this,"Searching...",Toast.LENGTH_LONG).show()

        val searchFriendsQuery = allUsersRef.orderByChild("fullname")
            .startAt(userInputText)
            .endAt(userInputText + "\uf8ff")

        val options: FirebaseRecyclerOptions<FindFriends> = FirebaseRecyclerOptions.Builder<FindFriends>()
            .setQuery(searchFriendsQuery,FindFriends::class.java)
            .build()

        val adapter = object: FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder>(options) {
            override fun onBindViewHolder(viewHolder: FindFriendsViewHolder, position: Int, model: FindFriends) {
                viewHolder.setProfilesInfo(model)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendsViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_display_layout, parent, false)
                return FindFriendsViewHolder(view)
            }
        }
        adapter.startListening()
        searchOutputList.adapter = adapter
    }
        class FindFriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView = itemView

        val friendsProfileImage: CircleImageView = mView.findViewById(R.id.find_users_profile_image)
        val friendsFullName: TextView = mView.findViewById(R.id.find_user_full_name)
        val friendsStatus: TextView = mView.findViewById(R.id.find_user_status)

        fun setProfilesInfo(friends: FindFriends){
            friendsFullName.text = friends.fullname
            friendsStatus.text = friends.status
            Glide.with(itemView.context)
                .load(friends.profileimage)
                .into(friendsProfileImage)
        }
    }
}
