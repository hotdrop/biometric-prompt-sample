package jp.hotdrop.biometricpromptsample

import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import timber.log.Timber
import java.util.concurrent.Executors

object BiometricPrompt {

    sealed class Result {
        object Success: Result()
        object UnsupportedAPILevel: Result()
        object UnsupportedHardware: Result()
        object NotHasBiometricsOnHardware: Result()
        object Cancel: Result()
    }

    fun available(context: Context): Boolean {
        return when (BiometricManager.from(context).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun auth(activity: FragmentActivity, onResultListener: (result: Result) -> Unit) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Timber.d("サポートされていないOS（AndroidM未満）")
            onResultListener(Result.UnsupportedAPILevel)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo
            .Builder()
            .setTitle("アプリ認証")
            .setDescription("この機能をご利用する場合は認証が必要です。")
            .setNegativeButtonText("PINコードで認証する")
            .build()

        val executor = Executors.newSingleThreadExecutor()
        BiometricPrompt(activity, executor, object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResultListener(Result.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // エラーコードはただの一例。必要に応じて増減する
                when (errorCode) {
                    BiometricPrompt.ERROR_HW_NOT_PRESENT, BiometricPrompt.ERROR_HW_UNAVAILABLE ->  {
                        Timber.d("指紋認証ハードウェアが搭載されていない。")
                        onResultListener(Result.UnsupportedHardware)
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        Timber.d("指紋が登録されていない。")
                        onResultListener(Result.NotHasBiometricsOnHardware)
                    }
                    else -> {
                        Timber.d("その他のコード=$errorCode")
                        onResultListener(Result.Cancel)
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("別の指紋や濡れていたり認証できなかった時に呼ばれる。")
                onResultListener(Result.Cancel)
            }
        }).authenticate(promptInfo)
    }
}