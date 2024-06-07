package com.example.logisticapp.di.executor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.logisticapp.R
import com.example.logisticapp.di.executor.recyclerview.RecyclerViewOrderAdapterExecutor
import com.example.logisticapp.di.logistics.LogisticMainActivity
import com.example.logisticapp.di.logistics.LogisticOrderActivity
import com.example.logisticapp.di.logistics.recyclerview.RecyclerViewOrderAdapter
import com.example.logisticapp.domain.models.Order
import com.example.logisticapp.domain.models.OrderRecycler
import com.example.logisticapp.domain.models.Point
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ExecutorMainActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var recyclerViewOrderAdapter: RecyclerViewOrderAdapterExecutor? = null
    private var orderList = mutableListOf<OrderRecycler>()

    private lateinit var uidOrderText: TextView
    private lateinit var startPointText: TextView
    private lateinit var finishPointText: TextView
    private lateinit var historyBtn: FloatingActionButton
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var cardView: CardView

    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executor_main)

        orderList = ArrayList()

        recyclerView = findViewById(R.id.orderRecyclerView)
        recyclerViewOrderAdapter = RecyclerViewOrderAdapterExecutor(this@ExecutorMainActivity, orderList)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = recyclerViewOrderAdapter

        uidOrderText = findViewById(R.id.uidOrderText)
        startPointText = findViewById(R.id.startPointText)
        finishPointText = findViewById(R.id.finishPointText)

        historyBtn = findViewById(R.id.historyBtn)

        historyBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@ExecutorMainActivity, ExecutorHistroyActivity::class.java))
        })

        swipe = findViewById(R.id.swipeRefreshLayout)

        swipe.setOnRefreshListener {
            prepareOrderList()
            swipe.isRefreshing = false
        }

        cardView = findViewById(R.id.cardView2)

        cardView.setOnClickListener(View.OnClickListener {
            if (orderList.size > 0) {
                val intent = Intent(this, ExecutorAllInfoActivity::class.java)
                intent.putExtra("order", orderList[0])
                startActivity(intent)
            }
        })



        prepareOrderList()
    }

    private fun prepareData() {
        if (orderList.size != 0) {
            uidOrderText.setText(orderList[0].UID)
            startPointText.setText(orderList[0].nameStart)
            finishPointText.setText(orderList[0].nameFinish)

            if (orderList[0].status != "Выполняется") {
                val myRef = databaseReference.child("orders").child(orderList[0].UID!!)

                orderList[0].status = "Выполняется"

                val updatedData = HashMap<String, Any>()
                updatedData["status"] = orderList[0].status!!

                myRef.updateChildren(updatedData)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {
                        // Обработка ошибки
                    }
            }
        }
    }

    // Функция для вычисления расстояния между двумя точками на поверхности Земли
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0  // Радиус Земли в километрах

        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        val dlon = lon2Rad - lon1Rad
        val dlat = lat2Rad - lat1Rad

        val a = sin(dlat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = R * c

        return distance
    }

    // Расширение для списка для генерации всех возможных перестановок
    fun <T> List<T>.permutations(): List<List<T>> {
        if (this.isEmpty()) return emptyList()
        if (this.size == 1) return listOf(this)

        val perms = mutableListOf<List<T>>()
        val toInsert = this[0]
        val subList = this.subList(1, this.size)

        val subPerms = subList.permutations()
        for (perm in subPerms) {
            for (i in 0..perm.size) {
                val newPerm = perm.toMutableList()
                newPerm.add(i, toInsert)
                perms.add(newPerm)
            }
        }
        return perms
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

                        val orderRecycler = OrderRecycler(
                            order.start,
                            order.finish,
                            order.nameStart,
                            order.nameFinish,
                            order.descProduct,
                            order.executor,
                            order.status,
                            orderId,
                            order.queue
                        )

                        filteredOrders.add(orderRecycler)
                    }
                }

                // Фильтруем заказы по статусу
                val ordersInProgress =
                    filteredOrders.filter { it.status == "Готов к исполнению" || it.status == "Выполняется" }

                if (ordersInProgress.size > 1 && ordersInProgress[0].queue == null) {
                    val perms = ordersInProgress.permutations()

                    // Вычисляем общее расстояние для каждой перестановки и выбираем оптимальную
                    val moscow = Point(55.755825, 37.617298)
                    var minDistance = Double.POSITIVE_INFINITY
                    var optimalOrder: List<OrderRecycler>? = null

                    for (perm in perms) {
                        var totalDistance = 0.0
                        for (i in 0 until perm.size) {
                            val order = perm[i]
                            totalDistance += distance(
                                moscow.lat,
                                moscow.lon,
                                order.start!!.lat,
                                order.start!!.lon
                            )
                            totalDistance += distance(
                                order.start!!.lat,
                                order.start!!.lon,
                                order.finish!!.lat,
                                order.finish!!.lon
                            )
                            totalDistance += distance(
                                order.finish!!.lat,
                                order.finish!!.lon,
                                moscow.lat,
                                moscow.lon
                            )
                        }
                        if (totalDistance < minDistance) {
                            minDistance = totalDistance
                            optimalOrder = perm
                        }
                    }

                    // Очищаем список заказов
                    orderList.clear()

                    // Добавляем отфильтрованные и отсортированные заказы обратно в список
                    optimalOrder?.let {
                        orderList.addAll(it)
                    }


                } else {
                    val sortedOrders = ordersInProgress.sortedBy { it.queue }

                    // Очищаем список заказов
                    orderList.clear()

                    // Добавляем отфильтрованные и отсортированные заказы обратно в список
                    sortedOrders.let {
                        orderList.addAll(it)
                    }
                }

                recyclerViewOrderAdapter?.notifyDataSetChanged()

                prepareData()
                queqe()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки запроса
                println("Ошибка получения пользователей: ${databaseError.message}")
            }
        })
    }

    private fun queqe() {
        for (i in 0 until orderList.size) {
            val myRef = databaseReference.child("orders").child(orderList[i].UID!!)

            orderList[i].queue = i + 1

            val updatedData = HashMap<String, Any>()
            updatedData["queue"] = orderList[i].queue!!

            myRef.updateChildren(updatedData)
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    // Обработка ошибки
                }
        }
    }
}