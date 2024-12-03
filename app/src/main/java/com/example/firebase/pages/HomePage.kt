package com.example.firebase.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebase.AuthState
import com.example.firebase.AuthViewModel
import com.example.firebase.StudentObj
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, database: FirebaseDatabase) {

    val authState = authViewModel.authState.observeAsState()

    val name = remember {
        mutableStateOf(TextFieldValue())
    }

    val age = remember {
        mutableStateOf(TextFieldValue())
    }
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("StudentInfo")
    val context = LocalContext.current



    LaunchedEffect (authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Page",fontSize = 32.sp)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Firebase Database")
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = name.value, onValueChange = {name.value = it},
            placeholder = {Text(text = "Enter your name")},
            modifier = Modifier.padding(15.dp))
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = age.value, onValueChange = {age.value = it},
            placeholder = {Text(text = "Enter your age")},
            modifier = Modifier.padding(15.dp))

        TextButton(onClick = {
            submitData(name.value.text, age.value.text, database)
            })
        {
            Text(text = "Save data")
        }


        TextButton(onClick = {
            val databaseReference = database.getReference("user")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Iterujemy po wszystkich dzieciach węzła "user"
                        val users = mutableListOf<String>()
                        for (child in snapshot.children) {
                            val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                            val age = child.child("age").getValue(String::class.java) ?: "Unknown"
                            users.add("$name ($age)")
                        }
                        Toast.makeText(context, "Users: ${users.joinToString(", ")}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "No data found", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to read data: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }) {
            Text(text = "Read data")
        }

        TextButton(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign out")
        }
    }
}


private fun submitData(
    name: String,
    age: String,
    database: FirebaseDatabase) {
    val myRef = database.getReference("user")

    // Push data to Firebase Realtime Database
    val userId = myRef.push().key
    val user = mapOf("name" to name, "age" to age)
    if (userId != null) {
        myRef.child(userId).setValue(user)
    }
}