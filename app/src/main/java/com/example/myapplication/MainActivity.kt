package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.annotation.NonNull
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


private var mFragmentManager: FragmentManager? = null
private val mLoadingFragment = loadingFragment()
private val mAccountFragment = AccountFragment()
private val mSettingsFragment = SettingsFragment()
private var mDrawerLayout: DrawerLayout? = null

private var mDatabase: DatabaseReference? = null

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setNavBars()

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
//    @Override
//    public fun onAuthStateChanged(@NonNull firebaseAuth: FirebaseAuth ) {

    //}

    fun changeActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    private fun setNavBars() {
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionbar = supportActionBar as ActionBar
        actionbar.setDisplayHomeAsUpEnabled(false)


        mDrawerLayout = findViewById(R.id.drawer_layout)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener{ item ->
            for (i in 0 until navigation.menu.size()) {
                navigation.menu.getItem(i).isCheckable = false
            }

            // item.isChecked = true

            mFragmentManager!!.popBackStackImmediate()

            val mFragmentTransaction1 = mFragmentManager!!.beginTransaction()


            when (item.itemId) {
                R.id.navigation_home -> {
                    actionbar.title = "first"
                    mFragmentTransaction1.replace(R.id.fragment_container, mAccountFragment)
                }

                R.id.navigation_scan -> {
                    actionbar.title = "second"
                    // TODO put camera fragment here replace the mAccountFragment with ur camera
                    mFragmentTransaction1.replace(R.id.fragment_container, mSettingsFragment)
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

        val sideNavigation = findViewById<NavigationView>(R.id.nav_view)
        sideNavigation.setNavigationItemSelectedListener { item ->
            item.setChecked(true)
            mDrawerLayout!!.closeDrawers()

            for (i in 0 until navigation.menu.size()) {
                navigation.menu.getItem(i).isCheckable = false
            }

            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            when (item.itemId) {
                R.id.sidebar_settings -> {
                    // actionbar.setTitle("Preferences")

                }
                R.id.sidebar_history -> {
                    // actionbar.setTitle("History")

                }
                R.id.sidebar_about -> {
                    // actionbar.setTitle("About")

                }

            }
            mFragmentTransaction.commit()
            mFragmentManager!!.executePendingTransactions()
            true
        }
    }
}
