package course.labs.fragmentslab

import course.labs.fragmentslab.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager



private var mFragmentManager: FragmentManager? = null
private val mLoadingFragment = LoadingFragment()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFragmentManager = supportFragmentManager

        val mFragmentTransaction = mFragmentManager!!.beginTransaction()
        mFragmentTransaction.add(R.id.fragment_container, mLoadingFragment)
        mFragmentTransaction.commit()
        mFragmentManager!!.executePendingTransactions()
    }
}
