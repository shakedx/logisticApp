package com.example.logisticapp.di.admin

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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegistrationActivity : AppCompatActivity() {

    companion object {
        lateinit var auth: FirebaseAuth
    }

    private lateinit var button: Button
    private lateinit var editTextLoginEmail: EditText
    private lateinit var editTextLoginPassword: EditText
    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var spinnerDrivers: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()


        button = findViewById(R.id.buttonRegister)
        editTextLoginEmail = findViewById(R.id.editTextEmail)
        editTextLoginPassword = findViewById(R.id.editTextPassword)
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        spinnerDrivers = findViewById(R.id.spinnerDrivers)

        val roleList = listOf("Админ", "Логист", "Исполнитель")
        val adapter = ArrayAdapter(this@RegistrationActivity, android.R.layout.simple_spinner_item, roleList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDrivers.adapter = adapter

        button.setOnClickListener(View.OnClickListener {
            registration(editTextLoginEmail.text.toString(), editTextLoginPassword.text.toString(),
                editTextFirstName.text.toString(), editTextLastName.text.toString(), spinnerDrivers.selectedItem.toString())
        })

    }

    fun registration(email: String, password: String, firstName: String, lastName: String, role:String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            OnCompleteListener {
                if (it.isSuccessful) {
                    val user = it.result?.user

                    val uid = user?.uid

                    if (uid != null) {
                        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://logisticapp-5ba6a-default-rtdb.europe-west1.firebasedatabase.app").reference
                        val myRef = databaseReference.child("users").child(uid)

                        val userMap = HashMap<String, Any>()
                        userMap["firstname"] = firstName
                        userMap["lastname"] = lastName
                        userMap["role"] = role

                        myRef.setValue(userMap)
                            .addOnSuccessListener {
                                editTextLoginEmail.setText("")
                                editTextLoginPassword.setText("")
                                editTextFirstName.setText("")
                                editTextLastName.setText("")

                                Toast.makeText(this@RegistrationActivity, "Пользователь создан", Toast.LENGTH_SHORT).show()

                                Log.d("WriteToDatabase", "Data successfully written to the database")
                            }
                            .addOnFailureListener { e ->
                                Log.w("WriteToDatabase", "Error writing data to the database", e)
                            }
                    }
                }
            })
    }
}
