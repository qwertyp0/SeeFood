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
        mFragmentTransaction.add(R.id.fragment_container, mAccountFragment)
        mFragmentTransaction.commit()
        mFragmentManager!!.executePendingTransactions()
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
            for (i in 0 until navigation.menu.size()) {
                navigation.menu.getItem(i).isCheckable = false
            }


            mFragmentManager!!.popBackStackImmediate()

            val mFragmentTransaction1 = mFragmentManager!!.beginTransaction()


            when (item.itemId) {
                R.id.navigation_home -> {
                    actionbar.title = "Home"
                    mFragmentTransaction1.replace(R.id.fragment_container, mAccountFragment)
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
