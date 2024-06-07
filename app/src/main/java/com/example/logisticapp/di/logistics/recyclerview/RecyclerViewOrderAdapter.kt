package com.example.logisticapp.di.logistics.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticapp.R
import com.example.logisticapp.di.logistics.LogisticAllInfoActivity
import com.example.logisticapp.di.logistics.LogisticMainActivity
import com.example.logisticapp.di.logistics.LogisticOrderActivity
import com.example.logisticapp.domain.models.OrderRecycler

class RecyclerViewOrderAdapter constructor(private val getActivity: LogisticOrderActivity, private val orderList: List<OrderRecycler>) :
    RecyclerView.Adapter<RecyclerViewOrderAdapter.MyViewHolder>() {

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
            val intent = Intent(getActivity, LogisticAllInfoActivity::class.java)
            intent.putExtra("order", orderList[position])
            getActivity.startActivity(intent)})
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uidText : TextView = itemView.findViewById(R.id.uidOrder)
        val startPointText : TextView = itemView.findViewById(R.id.startPoint)
        val finishPointText : TextView = itemView.findViewById(R.id.finishPoint)
        val status : TextView = itemView.findViewById(R.id.status)
        val cardView : CardView = itemView.findViewById(R.id.cardView)
    }

}