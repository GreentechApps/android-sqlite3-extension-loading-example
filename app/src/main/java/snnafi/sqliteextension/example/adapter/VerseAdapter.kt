package snnafi.sqliteextension.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import snnafi.sqliteextension.example.R
import snnafi.sqliteextension.example.model.Verse


class VerseAdapter(val verses: List<Verse>) :
    RecyclerView.Adapter<VerseAdapter.VerseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseHolder {
        return VerseHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VerseHolder, position: Int) {
        holder.bindView(verses[position])
    }

    override fun getItemCount(): Int {
        return verses.size
    }

    inner class VerseHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text = itemView.findViewById<TextView>(R.id.text);

        fun bindView(verse: Verse) {
            text.text = verse.toString()
            animateView(itemView)
        }

        private fun animateView(viewToAnimation: View) {
            if (viewToAnimation.animation == null) {
                val animation = AnimationUtils.loadAnimation(viewToAnimation.context, R.anim.scaled)
                viewToAnimation.animation = animation
            }
        }

    }
}