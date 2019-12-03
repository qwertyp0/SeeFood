package com.example.myapplication

import android.accounts.Account
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    //database stuff
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginBtn: Button? = null
    private var progressBar: ProgressBar? = null

    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()

        initializeUI()

        loginBtn!!.setOnClickListener { loginUserAccount() }
    }

    private fun loginUserAccount() {
        progressBar!!.visibility = View.VISIBLE
        val email: String
        val password: String
        email = userEmail!!.text.toString()
        password = userPassword!!.text.toString()

        // Todo : Retrieve email and password, make sure it's not empty
        val userId = mAuth!!.getCurrentUser()?.uid

        if (email.equals("") || password.equals("")) {
            Toast.makeText(applicationContext, "Login failed! Please make sure email and password are filled in", Toast.LENGTH_LONG).show()
        }
        else {

            // Todo : Sign in with given Email and Password
            mAuth!!.signInWithEmailAndPassword(email,password).
                addOnCompleteListener{task ->

                    if (task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra(UserID, userId)
                        startActivity(intent)

                    }
                    else {
                        Toast.makeText(applicationContext,"Email or password is incorrect. Please try again",Toast.LENGTH_LONG)
                    }
                }
            // Retrieve UID for Current User if Login successful and store in intent, for the key UserID
            // Start Intent DashboardActivity if Registration Successful

        }

    }

    private fun initializeUI() {
        userEmail = findViewById(R.id.email)
        userPassword = findViewById(R.id.password)

        loginBtn = findViewById(R.id.login)
        progressBar = findViewById(R.id.progressBar)
    }

    companion object {
        val TAG = "LAB 6"
        val UserMail = "com.example.tesla.myhomelibrary.UMail"
        val UserID = "com.example.tesla.myhomelibrary.UID"

    }
}

