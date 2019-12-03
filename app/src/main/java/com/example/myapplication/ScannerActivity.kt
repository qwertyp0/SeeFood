package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class ScannerActivity : AppCompatActivity(), View.OnClickListener {

    private var uiModel : UIModel? = null
    private var prevUIState : UIState? = null
    private var currUIState : UIState? = null
    private lateinit var cameraFragment : CameraFragment
    private lateinit var formFragment : FormFragment
    private lateinit var extendedFAB : ExtendedFloatingActionButton
    private lateinit var toolBar : MaterialToolbar
    private lateinit var appBarLayout : AppBarLayout
    private lateinit var gradientAppBar : RelativeLayout
    private lateinit var chip : Chip
    private var inflatedMenu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        // extended fab set up
        extendedFAB = findViewById(R.id.extended_fab)
        extendedFAB.setOnClickListener(this@ScannerActivity)

        // chip set up
        chip = findViewById(R.id.bottom_chip)

        // app bar set up
        toolBar = findViewById(R.id.tool_bar)
        toolBar.setOnClickListener(this@ScannerActivity)
        appBarLayout = findViewById(R.id.app_bar)
        gradientAppBar = findViewById(R.id.gradient_appbar)
        appBarLayout.background = null
        appBarLayout.outlineProvider = null
        toolBar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolBar.setNavigationOnClickListener { finish() }
        toolBar.setTitleTextColor(resources.getColor(R.color.white))


        // check permissions first
        if (!allPermissionsGranted())
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        else {
            cameraFragment = CameraFragment()
        }


        // set up ui model and fragments
        setUpUIModel()
        formFragment = FormFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, cameraFragment, cameraFragment.tag)
            .commit()

        // intial ui state
        uiModel?.setUIState(UIState.BARCODE)
        uiModel?.setNutritionLabel(NutritionLabel())
        uiModel?.setNutritionLabelExists(false)
    }

    // Override on click actions for views using main activity as the listener
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.extended_fab -> {
                val label = formFragment.newLabel()
                if (label == null) {
                    Snackbar.make(this.findViewById<FrameLayout>(R.id.fragment_container),
                        "Invalid fields, please double check",
                        Snackbar.LENGTH_LONG)
                        .show()
                } else {
                    val intent = Intent()
                    intent.putExtra("result", label)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            R.id.view_finder -> {
                if (currUIState == UIState.LABEL) {
                    cameraFragment.extractText()
                    uiModel?.setUIState(UIState.LOADING)
                }
            }
            else -> println("damn")
        }
    }

    // Set up UI model for main activity and start observing
    private fun setUpUIModel() {
        uiModel = ViewModelProviders.of(this).get(UIModel::class.java)
        val uiObserver = Observer<UIState?> {uiState ->
            prevUIState = currUIState
            currUIState = uiState
            updateAppBar()
            updateMenu()
            updateChip()
            updateContainer()
        }
        uiModel!!.uiState.observe(this, uiObserver)
    }

    // Replacing current fragment with correct one
    private fun updateContainer() {
        when (currUIState) {
            UIState.BARCODE, UIState.LABEL, UIState.LOADING -> {
                if (prevUIState == UIState.FORM) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, cameraFragment, cameraFragment.tag)
                        .commit()
                }
            }
            UIState.FORM -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, formFragment, formFragment.tag)
                    .commit()
            }
        }
    }

    // Update appbar, toggle appbar and fab visibility
    private fun updateAppBar() {
        toolBar.title = currUIState.toString().toLowerCase().capitalize()

        when (currUIState) {
            UIState.BARCODE, UIState.LABEL -> {
                appBarLayout.visibility = View.VISIBLE
                gradientAppBar.visibility = View.VISIBLE
                extendedFAB.visibility = View.GONE
            }
            UIState.LOADING -> {
                appBarLayout.visibility = View.VISIBLE
                gradientAppBar.visibility = View.VISIBLE
                extendedFAB.visibility = View.GONE
            }
            UIState.FORM -> {
                appBarLayout.visibility = View.GONE
                gradientAppBar.visibility = View.GONE
                extendedFAB.visibility = View.VISIBLE
            }
        }
    }

    private fun updateMenu() {
        when (currUIState) {
            UIState.BARCODE, UIState.LABEL -> {
                if (!inflatedMenu) {
                    toolBar.inflateMenu(R.menu.camera_toolbar_menu)
                    toolBar.setOnMenuItemClickListener {
                        when (it.title) {
                            "toggle" -> {
                                if (currUIState == UIState.BARCODE)
                                    uiModel?.setUIState(UIState.LABEL)
                                else if (currUIState == UIState.LABEL)
                                    uiModel?.setUIState(UIState.BARCODE)
                            }
                            "form" -> {
                                uiModel?.setUIState(UIState.FORM)
                            }
                            else -> println("Fuck")
                        }
                        true
                    }
                    inflatedMenu = true
                }
            }
            UIState.FORM, UIState.LOADING -> {
                if (inflatedMenu) {
                    toolBar.menu.clear()
                    inflatedMenu = false
                }
            }
        }
    }

    // set chip text
    private fun updateChip() {
        when (currUIState) {
            UIState.BARCODE -> chip.text = "Scanning for barcode"
            UIState.LABEL -> chip.text = "Tap screen to process label"
            UIState.LOADING -> chip.text = "Please wait..."
            UIState.FORM -> chip.text = "View is gone"
        }
    }

    // Override to close if permissions not granted when asked
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Snackbar.make(this.findViewById<FrameLayout>(R.id.fragment_container),
                    "Permissions not granted",
                    Snackbar.LENGTH_SHORT)
                    .show()
                finish()
            } else {
                cameraFragment = CameraFragment()
            }
        }
    }

    // Checks if permissions granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}
