package jp.hotdrop.biometricpromptsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(false)
        }

        returnButton.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, ResultActivity::class.java))
    }
}