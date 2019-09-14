package jp.hotdrop.biometricpromptsample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(false)
        }

        nextButton.setOnClickListener {
            AuthActivity.startForResult(this, REQUEST_CODE_TO_AUTH_ACTIVITY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TO_AUTH_ACTIVITY) {
            ResultActivity.start(this)
        }
    }

    companion object {
        const val REQUEST_CODE_TO_AUTH_ACTIVITY = 1000
    }
}
