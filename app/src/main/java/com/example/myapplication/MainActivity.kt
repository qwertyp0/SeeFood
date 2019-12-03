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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


private var mFragmentManager: FragmentManager? = null
private val mLoadingFragment = loadingFragment()
private val mAccountFragment = AccountFragment()
private val mSettingsFragment = SettingsFragment()
private val mHomeFragment = HomeFragment()
private val mFormFragment = FormFragment()



private var mDatabaseReference: DatabaseReference? = null
private var mDatabase: FirebaseDatabase? = null
private var mAuth: FirebaseAuth? = null
private var userId: String? = null


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initializing database stuff
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        userId = mAuth!!.getCurrentUser()?.uid.toString()


        setNavBars()

        mFragmentManager = supportFragmentManager

        val mFragmentTransaction = mFragmentManager!!.beginTransaction()
        /*
        mDatabaseReference?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                //if account settings is set up
                if (data.hasChild(userId!!)) {
                    mFragmentTransaction.add(R.id.fragment_container, mHomeFragment)
                    mFragmentTransaction.commit()
                }
                //if account setting is not set up redirect to account_fragment
                else {
                    mFragmentTransaction.add(R.id.fragment_container, mAccountFragment)
                    mFragmentTransaction.commit()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.i("TAG", "TAG"
            }
        })
        */

        mFragmentTransaction.add(R.id.fragment_container, mHomeFragment)
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
            val mFragmentTransaction1 = mFragmentManager!!.beginTransaction()
            when (item.itemId) {
                R.id.navigation_home -> {
                    mFragmentManager!!.popBackStackImmediate()
                    actionbar.title = "Home"
                    mFragmentTransaction1.replace(R.id.fragment_container, mHomeFragment)
                }
                R.id.settings -> {
                    mFragmentManager!!.popBackStackImmediate()
                    actionbar.title = "Settings"
                    mFragmentTransaction1.replace(R.id.fragment_container, mSettingsFragment)
                }

            }
            mFragmentTransaction1.commit()
            mFragmentManager!!.executePendingTransactions()
            true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(supportActionBar!!.title != "Home") changeActionBarTitle("Settings")
    }
}
