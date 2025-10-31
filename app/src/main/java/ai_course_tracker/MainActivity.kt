package com.example.automation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.automation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        // No action bar; each Fragment uses its own MaterialToolbar
    }
}
