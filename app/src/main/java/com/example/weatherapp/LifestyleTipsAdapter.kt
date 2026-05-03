package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//class LifestyleTipsAdapter(private var tipsList: List<LifestyleTip>) :
//    RecyclerView.Adapter<LifestyleTipsAdapter.LifestyleTipViewHolder>() {
//
//    class LifestyleTipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tipIcon: ImageView = itemView.findViewById(R.id.tipIcon)
//        val tipText: TextView = itemView.findViewById(R.id.tipText)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LifestyleTipViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_lifestyle_tip, parent, false)
//        return LifestyleTipViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: LifestyleTipViewHolder, position: Int) {
//        val tip = tipsList[position]
//        holder.tipIcon.setImageResource(tip.iconRes)
//        holder.tipText.text = tip.text
//    }
//
//    override fun getItemCount(): Int = tipsList.size
//
//    fun updateData(newTips: List<LifestyleTip>) {
//        tipsList = newTips
//        notifyDataSetChanged()
//    }
//}

data class LifestyleTip(
    val text: String,
    val iconRes: Int
)