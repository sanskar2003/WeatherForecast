package com.sanskar.weatherforecast

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.w3c.dom.Text


class settings : AppCompatActivity() {
    private lateinit var bottom_nav: BottomNavigationView
    private lateinit var switch: Switch
    private lateinit var linearLayout1: LinearLayout
    private lateinit var linearLayout2: LinearLayout
    private lateinit var custom_location: Text
    private lateinit var unitSystem: TextView
    private var selected_choice: Int = 0
    private lateinit var selectedItem: String
    private var curr_loc: String = ""
    private var checked: String? = "1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        bottom_nav = findViewById(R.id.bottom_nav)
        switch = findViewById(R.id.switch1)
        linearLayout1 = findViewById(R.id.linearLayout1)
        linearLayout2 = findViewById(R.id.linearLayout2)
        unitSystem = findViewById(R.id.unit_system)
        switch.isChecked = true
        linearLayout1.alpha = 0.5f
        val checked_home = intent.getStringExtra("checked_home").toString()
        val curr_location = intent.getStringExtra("curr_location").toString()
        unitSystem.text = intent.getStringExtra("unitSystem")
        if(unitSystem.text.equals("Imperial")){
            unitSystem.text = "Imperial"
            selected_choice = 1
            selectedItem = "Imperial"
        }
        else{
            unitSystem.text = "Metric"
            selected_choice = 0
            selectedItem = "Metric"
        }
        if(checked_home=="0"){
            switch.isChecked = false
            linearLayout1.alpha = 1.0f
        }
        else{
            switch.isChecked = true
            linearLayout1.alpha = 0.5f
        }
        checked = checked_home
        bottom_nav.selectedItemId = R.id.settings
        bottom_nav.setOnItemSelectedListener {
            if(curr_loc.equals("")){
                switch.isChecked = true
                linearLayout1.alpha = 0.5f
                checked = "1"
                curr_loc = curr_location
            }
            when (it.itemId) {
                R.id.home -> {
                    val intent = Intent(this@settings, MainActivity::class.java)
                    intent.putExtra("checked", checked)
                    intent.putExtra("curr_loc", curr_loc)
                    intent.putExtra("unitSystem", unitSystem.text)
                    startActivity(intent)
                    overridePendingTransition(0,0)
                    finish()
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> {
                    true
                }
            }
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                linearLayout1.alpha = 0.5f
                checked = "1"
            } else {
                linearLayout1.alpha = 1.0f
                checked = "0"
                curr_loc = ""
            }
        }
        linearLayout1.setOnClickListener {
            if(linearLayout1.alpha == 1.0f){
                val builder = AlertDialog.Builder(this)
                val input = EditText(this)
                input.hint = "Enter the location"
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)
                builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                    curr_loc = input.text.toString()
                })
                builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
                builder.show()
            }
        }
        linearLayout2.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Unit System")
            val items = arrayOf("Metric", "Imperial")
            builder.setSingleChoiceItems(items, selected_choice,
                DialogInterface.OnClickListener { _, arg1 -> // TODO Auto-generated method stub
                    selectedItem = items[arg1]
                    selected_choice = arg1
                })
            builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                unitSystem.text = selectedItem
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{ dialog, which -> dialog.cancel()})
            builder.show()
        }
    }
}
