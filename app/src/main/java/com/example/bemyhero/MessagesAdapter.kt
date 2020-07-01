package com.example.bemyhero

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class MessagesAdapter(messagesList: List<Messages>) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
    private var userMessages: List<Messages> = messagesList
    private lateinit var mAuth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference

    override fun getItemCount(): Int {
        return userMessages.size
    }

    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        val senderID: String? = mAuth.currentUser?.uid
        val messages: Messages = userMessages[position]

        val receiverID: String = messages.from.toString()
        val messageType: String? = messages.type

        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(receiverID)

        // Gets and displays your friend's profile image in the chat
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val receiverImage: String = dataSnapshot.child("profileimage").value.toString()
                    Glide.with(viewHolder.receiverProfileImage.context)
                        .load(receiverImage)
                        .placeholder(R.drawable.profile)
                        .into(viewHolder.receiverProfileImage)
                }
            }
            override fun onCancelled(databaseError: DatabaseError){}
        })

        if(messageType == "text"){
            viewHolder.receiverMessage.visibility = View.INVISIBLE
            viewHolder.receiverProfileImage.visibility = View.INVISIBLE

            // Displays your messages (you are the sender/ your friend is the receiver)
            if(receiverID == senderID){
                viewHolder.senderMessage.setBackgroundResource(R.drawable.sender_message_background)
                viewHolder.senderMessage.setTextColor(Color.WHITE)
                viewHolder.senderMessage.gravity = Gravity.LEFT
                viewHolder.senderMessage.text = messages.message
            }
            // Displays your friend's messages (he is the sender/ you are the receiver)
            else {
                viewHolder.senderMessage.visibility = View.INVISIBLE

                viewHolder.receiverProfileImage.visibility = View.VISIBLE
                viewHolder.receiverMessage.visibility = View.VISIBLE

                viewHolder.receiverMessage.setBackgroundResource(R.drawable.receiver_message_background)
                viewHolder.receiverMessage.setTextColor(Color.WHITE)
                viewHolder.receiverMessage.gravity = Gravity.LEFT
                viewHolder.receiverMessage.text = messages.message
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.users_message_layout,parent,false)
        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(view)
    }

    class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var mView: View = itemView

        val receiverMessage: TextView = mView.findViewById(R.id.receiver_message)
        val senderMessage: TextView = mView.findViewById(R.id.sender_message)
        val receiverProfileImage: CircleImageView = mView.findViewById(R.id.message_profile_image)

        fun setMessage(messages: Messages){

        }
    }
}