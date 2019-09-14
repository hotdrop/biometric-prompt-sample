package jp.hotdrop.biometricpromptsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        startBiometricPrompt()
    }

    private fun startBiometricPrompt() {
        BiometricPrompt.auth(this) {
            when (it) {
                BiometricPrompt.Result.Success -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                BiometricPrompt.Result.Cancel, BiometricPrompt.Result.UnsupportedAPILevel -> showSnackBar(R.string.biometric_unsupported_api_level)
                BiometricPrompt.Result.UnsupportedHardware -> showSnackBar(R.string.biometric_unsupported_hardware)
                BiometricPrompt.Result.NotHasBiometricsOnHardware -> showSnackBar(R.string.biometric_not_has_biometrics_on_hardware)
            }
        }
    }

    private fun showSnackBar(@StringRes res: Int) {
        Snackbar.make(snackbarArea, getString(res), 1000).also { snackbar ->
            snackbar.setAction(android.R.string.ok) { snackbar.dismiss() }
            snackbar.view.background = ContextCompat.getDrawable(this, R.drawable.shape_snackbar)
        }.show()
    }

    companion object {
        fun startForResult(activity: Activity, requestCode: Int) =
                activity.startActivityForResult(Intent(activity, AuthActivity::class.java), requestCode)
    }
}