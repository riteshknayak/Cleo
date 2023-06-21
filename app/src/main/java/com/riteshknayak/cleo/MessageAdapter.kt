package com.riteshknayak.cleo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.riteshknayak.cleo.MessageAdapter.MyViewHolder


class MessageAdapter(var messageList: List<Message>) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val chatView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item, null)
        return MyViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == Message.SENT_BY_ME) {
            holder.leftChatView.visibility = View.GONE
            holder.rightChatView.visibility = View.VISIBLE
            holder.rightTextView.text = message.message

            //Setting up copy Button
            holder.copyButton.setOnClickListener{
                val clipboard = getSystemService(holder.context , ClipboardManager::class.java) as ClipboardManager
                val clip = ClipData.newPlainText("label",message.message)
                clipboard.setPrimaryClip(clip)
            }

        } else {
            holder.rightChatView.visibility = View.GONE
            holder.leftChatView.visibility = View.VISIBLE
            holder.leftTextView.text = message.message

        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leftChatView: LinearLayout
        var rightChatView: LinearLayout
        var leftTextView: TextView
        var rightTextView: TextView
        var copyButton: ImageView
        var context: Context

        init {
            leftChatView = itemView.findViewById(R.id.left_chat_view)
            rightChatView = itemView.findViewById(R.id.right_chat_view)
            leftTextView = itemView.findViewById(R.id.left_chat_text_view)
            rightTextView = itemView.findViewById(R.id.right_chat_text_view)
            copyButton = itemView.findViewById(R.id.copy_button)
            context = itemView.context
        }
    }
}