package com.example.myapplication

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


private var mFragmentManager: FragmentManager? = null

private var mDatabaseReference: DatabaseReference? = null
private var mDatabase: FirebaseDatabase? = null
private var mAuth: FirebaseAuth? = null
private var userId: String? = null
private var cal: Calendar? = null
private var mDateView: TextView? = null

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.history_fragment, container, false)

        //initializing database stuff
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()

        //initializing today's date
        mDateView = view.findViewById(R.id.date) as TextView
        mDateView?.setText(getCurrentDate())
        cal = Calendar.getInstance()
        userId = mAuth!!.getCurrentUser()?.uid.toString()

        mFragmentManager = fragmentManager

        val dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal?.set(Calendar.YEAR, year)
                cal?.set(Calendar.MONTH, monthOfYear)
                cal?.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                mFragmentManager!!.popBackStackImmediate()
                updateDateInView()
            }
        }
        mDateView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(context!!,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal!!.get(Calendar.YEAR),
                    cal!!.get(Calendar.MONTH),
                    cal!!.get(Calendar.DAY_OF_MONTH)).show()

            }
        })


        //TODO The data is there, but it needs to be organized, maybe into a list?
        //Assume that account_settings are already initialized
        mDatabaseReference?.child(userId.toString())?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                //if there are scans for the user
                if (data.hasChild("daily_scans")) {
                    //TODO LOGAN, Look at my logic in makeBarGraph(HomeFragment.kt) to see how I indexed the values and their names
                    //should be a map of dates
                    //within the dates there should be a list of items (indexed 0-n)
                    //each item is a map of nutritional values
                    //
                    Log.i("History Fragment","Every scan the user made: " + data?.child("daily_scans").value)
                    var daily_scans = data?.child("daily_scans").value
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        return view
    }
    private fun getCurrentDate(): String {
        var sdf = SimpleDateFormat("EEE, MMM d, yyyy" )
        var currentDateandTime = sdf.format(Date())
        Log.i("Current Date","The current date is: "+ currentDateandTime)
        return currentDateandTime
    }
    private fun updateDateInView() {
        val myFormat = "EEE, MMM d, yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mDateView!!.text = sdf.format(cal!!.time)
    }
}