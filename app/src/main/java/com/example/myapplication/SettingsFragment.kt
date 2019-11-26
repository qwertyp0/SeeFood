package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager





//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.*

// import com.google.fireBase.database.R

class SettingsFragment : Fragment() {

    private var mFragmentManager: FragmentManager? = null
    private val mAccountFragment = AccountFragment()
    private val mAboutFragment = AboutFragment()
    private val mGraphSettingsFragment = GraphSettingsFragment()
    private val mHistoryFragment = HistoryFragment()

    private var accountEmail: TextView? = null
    private var account: Button? = null
    private var graphSettings: Button? = null
    private var history: Button? = null
    private var about: Button? = null
    private var logout: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        mFragmentManager = fragmentManager
        account = view.findViewById(R.id.account_button)
        graphSettings = view.findViewById(R.id.preferences_button)
        history = view.findViewById(R.id.history_button)
        about = view.findViewById(R.id.about_button)
        logout = view.findViewById(R.id.logout_button)
        accountEmail = view.findViewById(R.id.account_email)

        // Todo this will be grabbed from database and replaced here
        accountEmail!!.text = "joe.smith@gmail.com"

        // Todo make strings in R.vales.string for titles
        account!!.setOnClickListener {
            (activity as MainActivity?)!!.changeActionBarTitle(resources.getString(R.string.account_label))
            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            mFragmentTransaction.hide(this)
            mFragmentTransaction.addToBackStack("settingsFrag")
            mFragmentTransaction.replace(android.R.id.content, mAccountFragment)
            mFragmentTransaction.commit()
        }

        // Todo create history fragment
        history!!.setOnClickListener {
            (activity as MainActivity?)!!.changeActionBarTitle(resources.getString(R.string.history_label))
            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            mFragmentTransaction.hide(this)
            mFragmentTransaction.addToBackStack("settingsFrag")
            mFragmentTransaction.replace(android.R.id.content, mHistoryFragment)
            mFragmentTransaction.commit()
        }

        about!!.setOnClickListener {
            (activity as MainActivity?)!!.changeActionBarTitle(resources.getString(R.string.about_label))
            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            mFragmentTransaction.hide(this)
            mFragmentTransaction.addToBackStack("settingsFrag")
            mFragmentTransaction.replace(android.R.id.content, mAboutFragment)
            mFragmentTransaction.commit()
        }

        logout!!.setOnClickListener {

        }
        return view
    }

}