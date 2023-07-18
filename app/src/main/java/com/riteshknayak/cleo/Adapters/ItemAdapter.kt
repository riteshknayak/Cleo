package com.riteshknayak.cleo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.riteshknayak.cleo.Models.SubscriptionDataset
import com.riteshknayak.cleo.R

class ItemAdapter(private val itemList: ArrayList<SubscriptionDataset>) : RecyclerView.Adapter<ItemAdapter.itemHolder>() {
    private lateinit var mListener:OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }
    class itemHolder(itemView: View, listener: OnItemClickListener):RecyclerView.ViewHolder(itemView){
        val subName: TextView = itemView.findViewById(R.id.tvSubPlan)
        val price: TextView = itemView.findViewById(R.id.tvPlanPrice)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): itemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription,parent, false)
        return itemHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: itemHolder, position: Int) {
        val currentItem = itemList[position]
        holder.subName.text = currentItem.subscriptionName
        holder.price.text = currentItem.formattedPrice
    }
}