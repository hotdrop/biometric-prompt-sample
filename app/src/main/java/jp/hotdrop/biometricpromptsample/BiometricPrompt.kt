package jp.hotdrop.biometricpromptsample

import android.content.Context
import androidx.biometric.BiometricPrompt
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import timber.log.Timber
import java.util.concurrent.Executors

object BiometricPrompt {

    sealed class Result {
        object Success: Result()
        object UnsupportedHardware: Result()
        object NotHasBiometricsOnHardware: Result()
        object Cancel: Result()
        object Error: Result()
    }

    fun available(context: Context): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Timber.d("サポートされていないOS（AndroidM未満）")
            return false
        }

        return when (BiometricManager.from(context).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("生体認証が利用可能")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.d("端末に生体認証ハードウェアが搭載されていないなどで利用不可")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.d("端末に生体情報が登録されていない")
                false
            }
            else -> {
                Timber.d("その他のエラー")
                false
            }
        }
    }

    fun auth(activity: FragmentActivity, onResultListener: (result: Result) -> Unit) {

        val promptInfo = BiometricPrompt.PromptInfo
            .Builder()
            .setTitle(activity.getString(R.string.biometric_prompt_dialog_title))
            .setDescription(activity.getString(R.string.biometric_prompt_dialog_description))
            .setNegativeButtonText(activity.getString(R.string.biometric_prompt_dialog_negative_button_label))
            .build()

        val executor = Executors.newSingleThreadExecutor()
        BiometricPrompt(activity, executor, object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResultListener(Result.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // availableメソッドでチェックしたエラーはここでも検知できる
                when (errorCode) {
                    BiometricPrompt.ERROR_HW_NOT_PRESENT, BiometricPrompt.ERROR_HW_UNAVAILABLE ->  {
                        Timber.d("端末に生体認証ハードウェアが搭載されていないなどで利用不可")
                        onResultListener(Result.UnsupportedHardware)
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        Timber.d("端末に生体情報が登録されていない")
                        onResultListener(Result.NotHasBiometricsOnHardware)
                    }
                    BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        Timber.d("生体認証をキャンセル")
                        onResultListener(Result.Cancel)
                    }
                    else -> {
                        // サンプルでは適当に拾ってあとは全部elseにする。
                        // 一定時間経過によるタイムアウトやロックなど細かくerrorCodeが返ってくるのでプロダクトに入れる場合はその辺りちゃんとチェックしたほうがいい。
                        Timber.d("その他のコード=$errorCode")
                        onResultListener(Result.Error)
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("指に埃が付いていたり別の指紋を読み取るなどで認証できなかった時に呼ばれる。")
                onResultListener(Result.Error)
            }
        }).authenticate(promptInfo)
    }
}