package course.labs.fragmentslab

import course.labs.fragmentslab.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import androidx.appcompat.widget.Toolbar
// import android.support.v7.widget.Toolbar
import androidx.fragment.app.FragmentManager



private var mFragmentManager: FragmentManager? = null
private val mLoadingFragment = LoadingFragment()
private val mAccountFragment = AccountFragment()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbar()

        mFragmentManager = supportFragmentManager

        val mFragmentTransaction = mFragmentManager!!.beginTransaction()
        mFragmentTransaction.add(R.id.fragment_container, mLoadingFragment)
        mFragmentTransaction.commit()
        mFragmentManager!!.executePendingTransactions()
    }

    private fun setToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionbar = getSupportActionBar()
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        actionbar!!.setHomeAsUpIndicator(R.drawable.ic_home)
    }
}
