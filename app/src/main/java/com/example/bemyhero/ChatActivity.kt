package com.example.bemyhero

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var mToolbar: Toolbar
    private lateinit var sendImageButton: ImageButton
    private lateinit var sendMessageButton: ImageButton
    private lateinit var userMessageInput: EditText
    private lateinit var messagesList: RecyclerView

    private lateinit var messageReceiverID: String
    private lateinit var messageReceiverName: String

    private lateinit var chatReceiverFullName: TextView
    private lateinit var chatReceiverProfileImage: CircleImageView

    private lateinit var rootRef: DatabaseReference
    private lateinit var messageSenderID: String
    private lateinit var mAuth: FirebaseAuth

    private lateinit var saveCurrDateAndTime: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAuth = FirebaseAuth.getInstance()
        messageSenderID = mAuth.currentUser?.uid.toString()

        rootRef = FirebaseDatabase.getInstance().reference

        initializeActivityFields()
        displayReceiverInfo()

        sendMessageButton.setOnClickListener {
            sendMessageToFriend()
        }

    }

    private fun sendMessageToFriend() {
        val myMessage: String = userMessageInput.text.toString()

        if(TextUtils.isEmpty(myMessage)){
            Toast.makeText(this,"Please type something...",Toast.LENGTH_SHORT).show()
        }
        else {
            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

            val userMessageKey: DatabaseReference = rootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push()

            val messagePushID: String? = userMessageKey.key

            val currDateAndTime = getDate()
            val currDate = currDateAndTime.substring(0,10).replace("-","/")
            val currTime = currDateAndTime.substring(12)

            val messageTextBody = HashMap<String, String>()
            messageTextBody["message"] = myMessage
            messageTextBody["time"] = currTime
            messageTextBody["date"] = currDate
            messageTextBody["type"] = "text"
            messageTextBody["from:"] = messageSenderID

            val messageBodyDetails = HashMap<String, Any>()
            messageBodyDetails["$messageSenderRef/$messagePushID"] = messageTextBody
            messageBodyDetails["$messageReceiverRef/$messagePushID"] = messageTextBody

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this@ChatActivity,"Message Sent Successfully!",Toast.LENGTH_SHORT).show()
                }
                else {
                    val message: String = task.exception?.message.toString()
                    Toast.makeText(this@ChatActivity,"Error occurred: $message.\nPlease try again!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getDate(): String{
        val calendar: Calendar = Calendar.getInstance()
        val currDateAndTime = SimpleDateFormat("dd-MM-yyyy-HH:mm aa")
        saveCurrDateAndTime = currDateAndTime.format(calendar.time)
        return saveCurrDateAndTime
    }

    private fun displayReceiverInfo() {
        messageReceiverID = intent.extras?.get("FRIEND_ID").toString()
        messageReceiverName = intent.extras?.get("FRIEND_FULL_NAME").toString()

        chatReceiverFullName.text = messageReceiverName
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val receiverProfileImage: String = dataSnapshot.child("profileimage").value.toString()
                    Glide.with(this@ChatActivity)
                        .load(receiverProfileImage)
                        .placeholder(R.drawable.profile)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(chatReceiverProfileImage)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @SuppressLint("InflateParams")
    private fun initializeActivityFields() {
        mToolbar = findViewById(R.id.chat_page_toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar?.title = "Messages"

        userMessageInput = findViewById(R.id.input_message)
        sendImageButton = findViewById(R.id.send_image_button)
        sendMessageButton = findViewById(R.id.send_message_button)

        messagesList = findViewById(R.id.messages_list)
        messagesList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        messagesList.layoutManager = linearLayoutManager

        // Added chat_custom_bar.xml to ChatActivity.kt
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowCustomEnabled(true)
        val layoutInflater: LayoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView: View = layoutInflater.inflate(R.layout.chat_custom_bar,null)
        actionBar?.customView = actionBarView

        chatReceiverFullName = findViewById(R.id.custom_profile_name)
        chatReceiverProfileImage = findViewById(R.id.custom_profile_image)
    }
}
