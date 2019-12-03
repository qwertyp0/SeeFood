package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainEntryActivity : AppCompatActivity() {

    internal var registerBtn: Button? = null
    internal var loginBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_entry)

        initializeViews()

        registerBtn!!.setOnClickListener {
            val intent = Intent(this@MainEntryActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }
        loginBtn!!.setOnClickListener {
            val intent = Intent(this@MainEntryActivity, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }

    private fun initializeViews() {
        registerBtn = findViewById(R.id.register)
        loginBtn = findViewById(R.id.login)
    }
}

