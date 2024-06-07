package com.example.logisticapp.di.executor.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticapp.R
import com.example.logisticapp.di.executor.ExecutorAllInfoActivity
import com.example.logisticapp.di.executor.ExecutorHistroyActivity
import com.example.logisticapp.di.executor.ExecutorMainActivity
import com.example.logisticapp.domain.models.OrderRecycler

class RecyclerViewOrderHistoryAdapterExecutor constructor(private val getActivity: ExecutorHistroyActivity, private val orderList: List<OrderRecycler>) :
    RecyclerView.Adapter<RecyclerViewOrderHistoryAdapterExecutor.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_order_list_item, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.uidText.text = "UID: ${orderList[position].UID}"
        holder.startPointText.text = "Начальная точка: ${orderList[position].nameStart}"
        holder.finishPointText.text = "Конечная точка: ${orderList[position].nameFinish}"
        holder.status.text = "Статус: ${orderList[position].status}"

        holder.cardView.setOnClickListener(View.OnClickListener {
            val intent = Intent(getActivity, ExecutorAllInfoActivity::class.java)
            intent.putExtra("order", orderList[position])
            getActivity.startActivity(intent)
        })
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uidText : TextView = itemView.findViewById(R.id.uidOrder)
        val startPointText : TextView = itemView.findViewById(R.id.startPoint)
        val finishPointText : TextView = itemView.findViewById(R.id.finishPoint)
        val status : TextView = itemView.findViewById(R.id.status)
        val cardView : CardView = itemView.findViewById(R.id.cardView)
    }

}