package com.example.logisticapp.di

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.logisticapp.MainActivity
import com.example.logisticapp.R
import com.example.logisticapp.di.admin.RegistrationActivity
import com.example.logisticapp.di.executor.ExecutorMainActivity
import com.example.logisticapp.di.logistics.LogisticMainActivity
import com.example.logisticapp.di.logistics.LogisticOrderActivity
import com.example.logisticapp.domain.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yandex.mapkit.MapKitFactory

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var auth: FirebaseAuth
    }

    private lateinit var button: Button
    private lateinit var editTextLoginEmail: EditText
    private lateinit var editTextLoginPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("b8e34b90-8bcb-4226-a28a-691c1c5e5325")

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        button = findViewById(R.id.buttonLogin)
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail)
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword)

        button.setOnClickListener(View.OnClickListener {
            val email = editTextLoginEmail.text.toString()
            val password = editTextLoginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    OnCompleteListener {
                        if (it.isSuccessful) {
                            onCheckRole(auth.uid.toString())
                        }
                    }).addOnFailureListener(OnFailureListener {
                        Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                })
            }
        })
    }

    fun onCheckRole(uid: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference
        val myRef = databaseReference.child("users").child(uid)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    when (user.role) {
                        "Админ" -> startActivity(Intent(this@LoginActivity, RegistrationActivity::class.java))
                        "Логист" -> startActivity(Intent(this@LoginActivity, LogisticOrderActivity::class.java))
                        "Исполнитель" -> startActivity(Intent(this@LoginActivity, ExecutorMainActivity::class.java))

                        else -> {
                            println("Такой роли не существует")
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибок
                Toast.makeText(this@LoginActivity, "Ошибка при получении данных из базы данных", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startActivityCustom(intentClass: Any) {
        startActivity(Intent(this@LoginActivity, intentClass::class.java))

    }
}