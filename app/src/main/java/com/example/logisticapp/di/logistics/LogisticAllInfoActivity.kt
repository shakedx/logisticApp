package com.example.logisticapp.di.logistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.logisticapp.R
import com.example.logisticapp.domain.models.OrderRecycler
import com.example.logisticapp.domain.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class LogisticAllInfoActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    val pointArr: ArrayList<com.example.logisticapp.domain.models.Point> = ArrayList()
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference


    private lateinit var editTextPickupLocation: EditText
    private lateinit var editTextDeliveryLocation: EditText
    private lateinit var editTextInfProduct: EditText
    private lateinit var executorText: TextView
    private lateinit var spinnerDrivers: Spinner
    private lateinit var order: OrderRecycler
    private lateinit var saveBtn: Button
    private lateinit var buttonRemoveOrder: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistic_all_info)

        order = intent.getSerializableExtra("order") as OrderRecycler

        mapView = findViewById(R.id.mapview)

        editTextPickupLocation = findViewById(R.id.editTextPickupLocation)
        editTextDeliveryLocation = findViewById(R.id.editTextDeliveryLocation)
        editTextInfProduct = findViewById(R.id.editTextInfProduct)
        executorText = findViewById(R.id.executorText)
        spinnerDrivers = findViewById(R.id.spinnerDrivers)

        saveBtn = findViewById(R.id.buttonSaveOrder)
        buttonRemoveOrder = findViewById(R.id.buttonRemoveOrder)

        val imageProviderLoad = ImageProvider.fromResource(this, R.drawable.load)
        val imageProviderUnload = ImageProvider.fromResource(this, R.drawable.unload)
        val placemark = mapView.map.mapObjects.addPlacemark().apply {
            geometry = Point(order.start!!.lat, order.start!!.lon)
            setIcon(imageProviderLoad)
        }
        val placemark2 = mapView.map.mapObjects.addPlacemark().apply {
            geometry = Point(order.finish!!.lat, order.finish!!.lon)
            setIcon(imageProviderUnload)
        }

        saveBtn.setOnClickListener(View.OnClickListener {
            val myRef = databaseReference.child("orders").child(order.UID!!)

            order.status = spinnerDrivers.selectedItem.toString()
            order.descProduct = editTextInfProduct.text.toString()

            val updatedData = HashMap<String, Any>()
            updatedData["status"] = order.status!!
            updatedData["descProduct"] = order.descProduct!!

            myRef.updateChildren(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this@LogisticAllInfoActivity, "Заказ изменен", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LogisticAllInfoActivity, LogisticOrderActivity::class.java))
                }
                .addOnFailureListener {
                    // Обработка ошибки
                }
        })

        if (order.status == "Выполняется" || order.status == "Выполнено") {
            buttonRemoveOrder.isVisible = false
        }

        buttonRemoveOrder.setOnClickListener(View.OnClickListener {
            val myRef = databaseReference.child("orders").child(order.UID!!)

            myRef.removeValue()

            Toast.makeText(this@LogisticAllInfoActivity, "Заказ удален", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LogisticAllInfoActivity, LogisticOrderActivity::class.java))
        })

        prepareDataView()
    }

    private fun prepareDataView() {
        val myRef = databaseReference.child("users").child(order.executor!!)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user != null) {
                    executorText.text = "${user.lastname} ${user.firstname}"

                    editTextPickupLocation.setText(order.nameStart)
                    editTextDeliveryLocation.setText(order.nameFinish)
                    editTextInfProduct.setText(order.descProduct)

                }

                val statusList = listOf("Готов к исполнению", "Выполняется", "Выполнено")
                val adapter = ArrayAdapter(this@LogisticAllInfoActivity, android.R.layout.simple_spinner_item, statusList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDrivers.adapter = adapter

                // Установка выбранного элемента в Spinner на основе order.status
                order.status?.let { status ->
                    val selectedIndex = statusList.indexOf(status)
                    if (selectedIndex != -1) {
                        spinnerDrivers.setSelection(selectedIndex)
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки запроса
                println("Ошибка получения пользователей: ${databaseError.message}")
            }
        })

    }
}