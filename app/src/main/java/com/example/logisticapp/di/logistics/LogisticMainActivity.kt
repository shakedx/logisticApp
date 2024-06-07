package com.example.logisticapp.di.logistics

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.logisticapp.R
import com.example.logisticapp.domain.models.Order
import com.example.logisticapp.domain.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.lang.Exception
import java.util.UUID


class LogisticMainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    val pointArr: ArrayList<com.example.logisticapp.domain.models.Point> = ArrayList()
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference


    private lateinit var editTextPickupLocation: EditText
    private lateinit var editTextDeliveryLocation: EditText
    private lateinit var editTextInfProduct: EditText
    private lateinit var spinnerDrivers: Spinner
    private lateinit var buttonClearPoints: Button

    private lateinit var saveBtn: Button

    val usersList = mutableListOf<String>()
    val usersMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistic_main)
        mapView = findViewById(R.id.mapview)

        mapView.map.addInputListener(inputListener)

        editTextPickupLocation = findViewById(R.id.editTextPickupLocation)
        editTextDeliveryLocation = findViewById(R.id.editTextDeliveryLocation)
        editTextInfProduct = findViewById(R.id.editTextInfProduct)
        spinnerDrivers = findViewById(R.id.spinnerDrivers)
        saveBtn = findViewById(R.id.buttonCreateOrder)
        buttonClearPoints = findViewById(R.id.buttonClearPoints)

        val myRef = databaseReference.child("users")

        val query: Query = myRef.orderByChild("role").equalTo("Исполнитель")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        val userName = "${user.lastname} ${user.firstname}"
                        val userId = userSnapshot.key ?: "Unknown"
                        usersList.add(userName)
                        usersMap[userName] = userId
                    }
                }

                val adapter = ArrayAdapter<String>(this@LogisticMainActivity, android.R.layout.simple_spinner_item, usersList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDrivers.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки запроса
                println("Ошибка получения пользователей: ${databaseError.message}")
            }
        })

        saveBtn.setOnClickListener(View.OnClickListener {
            saveOrder()
        })

        buttonClearPoints.setOnClickListener(View.OnClickListener {
            pointArr.clear()
            mapView.map.mapObjects.clear()
        })
    }

    private fun saveOrder() {
        try {
            if (pointArr.size != 2) {
                throw Exception("Обязательно должна быть точка загрузки и разгрузки")
            }

            val order: Order = Order()

            order.start = pointArr.get(0)
            order.finish = pointArr.get(1)

            order.nameStart = editTextPickupLocation.text.toString()
            order.nameFinish = editTextDeliveryLocation.text.toString()
            order.descProduct = editTextInfProduct.text.toString()

            println(order.descProduct)

            order.executor = usersMap.get(spinnerDrivers.selectedItem.toString())
            order.status = "Готов к исполнению"

            val myRef = databaseReference.child("orders").child(UUID.randomUUID().toString())

            myRef.setValue(order)
                .addOnSuccessListener {
                    editTextPickupLocation.setText("")
                    editTextDeliveryLocation.setText("")
                    editTextInfProduct.setText("")
                    pointArr.clear()
                    mapView.map.mapObjects.clear()

                    Toast.makeText(this@LogisticMainActivity, "Заказ создан", Toast.LENGTH_SHORT).show()

                    Log.d("WriteToDatabase", "Data successfully written to the database")
                }
                .addOnFailureListener { e ->
                    Log.w("WriteToDatabase", "Error writing data to the database", e)
                }



        } catch (e: Exception) {
            Toast.makeText(this@LogisticMainActivity, e.message, Toast.LENGTH_SHORT).show()

        }
    }

    val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            if (pointArr.size < 2) {
                addPlacemarkToMap(point)
            }
        }

        override fun onMapLongTap(map: Map, point: Point) {
            // Handle long tap ...
        }
    }

    private fun addPlacemarkToMap(point: Point) {
        val map: Map = mapView.map

        // Создаем маркер
        val placemark = map.mapObjects.addPlacemark(point)

        // Устанавливаем изображение маркера (по желанию)
        if (pointArr.size > 0) {
            placemark.setIcon(ImageProvider.fromResource(this@LogisticMainActivity, R.drawable.unload))
        } else {
            placemark.setIcon(ImageProvider.fromResource(this@LogisticMainActivity, R.drawable.load))
        }

        // Передаем некоторые данные (по желанию)
        placemark.userData = "Some data"

        // Обновляем карту
        map.move(
            CameraPosition(point, 6f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 0.5f),
            null
        )

        pointArr.add(com.example.logisticapp.domain.models.Point(point.latitude, point.longitude));
    }



    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}