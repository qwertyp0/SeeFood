package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


private var mFragmentManager: FragmentManager? = null
private val mLoadingFragment = loadingFragment()
private val mAccountFragment = AccountFragment()

private var mDatabase: DatabaseReference? = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBottomNav()

        mFragmentManager = supportFragmentManager

        val mFragmentTransaction = mFragmentManager!!.beginTransaction()
        mFragmentTransaction.add(R.id.fragment_container, mLoadingFragment)
        mFragmentTransaction.commit()
        mFragmentManager!!.executePendingTransactions()
    }

    // going to need to start the firbase

//    protected fun onStart() {
//        super.onStart()
//
//        FirebaseAuth.getInstance()
//        //.add
//    }


    // I think this is where you would do user oauth
    @Override
    public fun onAuthStateChanged(@NonNull firebaseAuth: FirebaseAuth ) {

    }

    private fun setBottomNav() {
        // val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        // actionbar.setHomeAsUpIndicator(R.drawable.)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener{ item ->
            for (i in 0 until navigation.menu.size()) {
                navigation.menu.getItem(i).isCheckable = false
            }

            item.isChecked = true

            val mFragmentTransaction1 = mFragmentManager!!.beginTransaction()

            when (item.itemId) {
                R.id.navigation_home -> {
                    actionbar.title = "first"
                    mFragmentTransaction1.replace(R.id.fragment_container, mAccountFragment)
                }
                R.id.navigation_scan -> {
                    actionbar.title = "second"
                    mFragmentTransaction1.replace(R.id.fragment_container, mAccountFragment)
                }
                R.id.navigation_account -> {
                    actionbar.title = "third"
                    mFragmentTransaction1.replace(R.id.fragment_container, mAccountFragment)
                }
            }

            mFragmentTransaction1.commit()
            mFragmentManager!!.executePendingTransactions()

            true
        }
    }
}
