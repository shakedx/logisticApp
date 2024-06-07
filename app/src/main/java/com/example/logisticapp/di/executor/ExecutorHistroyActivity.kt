package com.example.logisticapp.di.executor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticapp.R
import com.example.logisticapp.di.executor.recyclerview.RecyclerViewOrderAdapterExecutor
import com.example.logisticapp.di.executor.recyclerview.RecyclerViewOrderHistoryAdapterExecutor
import com.example.logisticapp.domain.models.Order
import com.example.logisticapp.domain.models.OrderRecycler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ExecutorHistroyActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var recyclerViewOrderAdapter: RecyclerViewOrderHistoryAdapterExecutor? = null
    private var orderList = mutableListOf<OrderRecycler>()

    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executor_histroy)

        orderList = ArrayList()

        recyclerView = findViewById(R.id.orderRecyclerView)
        recyclerViewOrderAdapter = RecyclerViewOrderHistoryAdapterExecutor(this@ExecutorHistroyActivity, orderList)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewOrderAdapter

        prepareOrderList()

    }

    private fun prepareOrderList() {
        val myRef = databaseReference.child("orders")

        val queryExecutor: Query = myRef.orderByChild("executor").equalTo(FirebaseAuth.getInstance().currentUser?.uid)

        queryExecutor.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val filteredOrders = mutableListOf<OrderRecycler>()

                for (userSnapshot in dataSnapshot.children) {
                    val order = userSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        val orderId = userSnapshot.key ?: "Unknown"

                        var orderRecycler : OrderRecycler = OrderRecycler(order.start, order.finish,
                            order.nameStart, order.nameFinish, order.descProduct, order.executor, order.status,
                            orderId, order.queue)

                        filteredOrders.add(orderRecycler)
                    }
                }

                val ordersInProgress = filteredOrders.filter { it.status == "Выполнено"}
                orderList.addAll(ordersInProgress)


                recyclerViewOrderAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки запроса
                println("Ошибка получения пользователей: ${databaseError.message}")
            }
        })
    }
}