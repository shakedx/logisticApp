package com.example.logisticapp.di.logistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.logisticapp.R
import com.example.logisticapp.di.admin.RegistrationActivity
import com.example.logisticapp.di.logistics.recyclerview.RecyclerViewOrderAdapter
import com.example.logisticapp.domain.models.Order
import com.example.logisticapp.domain.models.OrderRecycler
import com.example.logisticapp.domain.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class LogisticOrderActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var recyclerViewOrderAdapter: RecyclerViewOrderAdapter? = null
    private var orderList = mutableListOf<OrderRecycler>()
    private lateinit var swipe: SwipeRefreshLayout

    private lateinit var createOrderBtn: FloatingActionButton
    private lateinit var countAllOrderText: TextView
    private lateinit var countProcessOrderText: TextView

    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistic_order)

        orderList = ArrayList()

        recyclerView = findViewById(R.id.orderRecyclerView)
        recyclerViewOrderAdapter = RecyclerViewOrderAdapter(this@LogisticOrderActivity, orderList)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewOrderAdapter

        createOrderBtn = findViewById(R.id.createOrder)

          countProcessOrderText = findViewById(R.id.countProcessOrderText)
          countAllOrderText = findViewById(R.id.countAllOrderText)


        createOrderBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@LogisticOrderActivity, LogisticMainActivity::class.java))
        })

        swipe = findViewById(R.id.swipeRefreshLayout)

        swipe.setOnRefreshListener {
            orderList.clear()
            prepareOrderList()
            swipe.isRefreshing = false
        }

        prepareOrderList()
    }

    private fun prepareOrderList() {
        val myRef = databaseReference.child("orders")


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val order = userSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        val orderId = userSnapshot.key ?: "Unknown"

                        var orderRecycler : OrderRecycler = OrderRecycler(order.start, order.finish,
                            order.nameStart, order.nameFinish, order.descProduct, order.executor, order.status,
                            orderId, null)

                        orderList.add(orderRecycler)
                    }
                }

                recyclerViewOrderAdapter!!.notifyDataSetChanged()
                countAllOrderText.setText(orderList.size.toString())

                val ordersInProgress = orderList.filter { it.status == "Выполняется"}
                countProcessOrderText.setText(ordersInProgress.size.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки запроса
                println("Ошибка получения пользователей: ${databaseError.message}")
            }
        })
    }
}