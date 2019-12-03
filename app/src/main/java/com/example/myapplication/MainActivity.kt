package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import android.util.Log
import androidx.annotation.NonNull
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private var mFragmentManager: FragmentManager? = null
    private val mLoadingFragment = loadingFragment()
    private val mAccountFragment = AccountFragment()
    private val mSettingsFragment = SettingsFragment()
    private val mHomeFragment = HomeFragment()
    private var mDrawerLayout: DrawerLayout? = null


    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        setNavBars()

        mFragmentManager = supportFragmentManager

        val mFragmentTransaction = mFragmentManager!!.beginTransaction()
        mFragmentTransaction.add(R.id.fragment_container, mHomeFragment)
        mFragmentTransaction.commit()
        mFragmentManager!!.executePendingTransactions()

        var isFirstTimeUser: Boolean? = false

        //The logic below will be for first time user's only
        //Upon log in, they will first be directed to account settings
        mDatabaseReference?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                if(dataSnapshot.hasChild("account_settings")){
                    mFragmentTransaction.replace(R.id.fragment_container,mAccountFragment)
                    mFragmentTransaction.commit()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    fun changeActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    private fun setNavBars() {
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionbar = supportActionBar as ActionBar
        actionbar.setDisplayHomeAsUpEnabled(false)


        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener{ item ->

            mFragmentManager!!.popBackStackImmediate()

            val mFragmentTransaction1 = mFragmentManager!!.beginTransaction()

            when (item.itemId) {
                R.id.navigation_home -> {
                    actionbar.title = "Home"
                    mFragmentTransaction1.replace(R.id.fragment_container, mHomeFragment)
                }

                R.id.navigation_scan -> {

                    // TODO start new activity here for scanner

                }
                R.id.settings -> {
                    actionbar.title = "Settings"

                    mFragmentTransaction1.replace(R.id.fragment_container, mSettingsFragment)
                }
            }

            mFragmentTransaction1.commit()
            mFragmentManager!!.executePendingTransactions()

            true
        }

    }
}
