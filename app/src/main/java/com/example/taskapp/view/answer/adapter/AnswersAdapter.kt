package com.example.taskapp.view.answer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskapp.R
import com.example.taskapp.databinding.ItemAnswerBinding
import com.example.taskapp.model.data_model.CustomAnswerData

class AnswersAdapter(
    private val answerList: ArrayList<CustomAnswerData>,
    inline val onItemClicked: (CustomAnswerData) -> Unit
) : RecyclerView.Adapter<AnswersAdapter.ViewHolder>() {

    lateinit var context: Context
    var clickable: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ItemAnswerBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, answerList[position])
    }

    override fun getItemCount(): Int {
        return answerList.size
    }

    inner class ViewHolder(private val itemViewBinding: ItemAnswerBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(context: Context, data: CustomAnswerData) {
            itemViewBinding.run {
                tvAnswer.text = data.answer
                cvAnswerContainer.setOnClickListener {
                    if(!clickable)
                        return@setOnClickListener
                    clickable = false
                    cvAnswerContainer.setCardBackgroundColor(
                        if (data.isCorrect) context.resources.getColor(
                            R.color.green
                        ) else context.resources.getColor(R.color.red)
                    )
                    tvAnswer.setTextColor(context.resources.getColor(R.color.white))
                    onItemClicked.invoke(data)
                }
            }
        }
    }
}