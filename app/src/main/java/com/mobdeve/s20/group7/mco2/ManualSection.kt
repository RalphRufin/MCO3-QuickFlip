package com.mobdeve.s20.group7.mco2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ManualSection(
    val title: String,
    val content: String
)

class ManualAdapter(private val sections: List<ManualSection>) :
    RecyclerView.Adapter<ManualAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.sectionTitle)
        val contentText: TextView = view.findViewById(R.id.sectionContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manual_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sections[position]
        holder.titleText.text = section.title
        holder.contentText.text = section.content
    }

    override fun getItemCount() = sections.size
}