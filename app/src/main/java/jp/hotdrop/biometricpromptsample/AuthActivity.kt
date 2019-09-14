package jp.hotdrop.biometricpromptsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        initView()
    }

    private fun initView() {
        if (BiometricPrompt.available(this)) {
            startBiometricPrompt()
        }
        initPinCodeEditView()
    }

    private fun startBiometricPrompt() {
        BiometricPrompt.auth(this) {
            when (it) {
                BiometricPrompt.Result.Success -> onSuccessAuth()
                BiometricPrompt.Result.UnsupportedHardware -> onFailureAuthBiometricPrompt(R.string.auth_biometric_unsupported_hardware)
                BiometricPrompt.Result.NotHasBiometricsOnHardware -> onFailureAuthBiometricPrompt(R.string.auth_biometric_not_has_biometrics_on_hardware)
                BiometricPrompt.Result.Cancel -> onFailureAuthBiometricPrompt(R.string.auth_biometric_cancel)
                else -> onFailureAuthBiometricPrompt(R.string.auth_biometric_failure)
            }
        }
    }

    private fun initPinCodeEditView() {

        // pinCode1は先頭なので設定する必要はない
        setDeleteKeyEvent(pinCode2, pinCode1)
        setDeleteKeyEvent(pinCode3, pinCode2)
        setDeleteKeyEvent(pinCode4, pinCode3)

        createPinCodeObservables()
    }

    private fun setDeleteKeyEvent(currentView: EditText, previousView: EditText) {
        currentView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.text.isNullOrEmpty()) {
                previousView.requestFocus()
            }
            false
        }
    }

    private val compositeDisposable = CompositeDisposable()
    private fun createPinCodeObservables() {

        fun observablePinCode1(): Observable<CharSequence> = pinCode1.textChanges().skip(1).map { getPinWithFocus(it.toString(), pinCode2) }
        fun observablePinCode2(): Observable<CharSequence> = pinCode2.textChanges().skip(1).map { getPinWithFocus(it.toString(), pinCode3) }
        fun observablePinCode3(): Observable<CharSequence> = pinCode3.textChanges().skip(1).map { getPinWithFocus(it.toString(), pinCode4) }
        fun observablePinCode4(): Observable<CharSequence> = pinCode4.textChanges().skip(1).map { getPinWithFocus(it.toString(), null) }

        // このコードは入力したPINコードが誤っていても無条件で無限に施行することができる。
        // 実際のプロダクトコードでこれはやばいので回数やタイムラグを設ける必要がある。
        // サンプルコードなのでプロダクトではコピペで使わないで欲しい。
        Observables.combineLatest(
                observablePinCode1(),
                observablePinCode2(),
                observablePinCode3(),
                observablePinCode4()
        ) { pin1, pin2, pin3, pin4 ->
            String.format("%s%s%s%s", pin1, pin2, pin3, pin4)
        }.subscribeBy(
                onNext = {
                    // 4桁全部入力されてからPINが正しいかチェックしたいのでここで桁数判定する。
                    if (it.length == 4) {
                        authPinCode(it)
                    }
                }
        ).addTo(compositeDisposable)
    }

    private fun getPinWithFocus(currentText: CharSequence, nextView: EditText?): CharSequence {
        return if (currentText.isNotEmpty() && currentText.isDigitsOnly()) {
            nextView?.requestFocus()
            errorMessage.visibility = View.GONE
            currentText
        } else {
            ""
        }
    }

    private fun authPinCode(inputPin: String) {
        if (inputPin == pinCode.toString()) {
            onSuccessAuth()
        } else {
            onFailureAuthWithPinCode()
        }
    }

    private fun onSuccessAuth() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onFailureAuthWithPinCode() {
        errorMessage.visibility = View.VISIBLE
        pinCode1.text.clear()
        pinCode2.text.clear()
        pinCode3.text.clear()
        pinCode4.text.clear()
        pinCode1.requestFocus()
    }

    private fun onFailureAuthBiometricPrompt(@StringRes res: Int) {
        showSnackBar(res)
    }

    private fun showSnackBar(@StringRes res: Int) {
        Snackbar.make(snackbarArea, getString(res), 5000).also { snackbar ->
            snackbar.setAction(android.R.string.ok) { snackbar.dismiss() }
            snackbar.view.background = ContextCompat.getDrawable(this, R.drawable.shape_snackbar)
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    companion object {
        // 本当はこれはユーザーが事前に設定しておくもの
        const val pinCode = 3685
        fun startForResult(activity: Activity, requestCode: Int) =
                activity.startActivityForResult(Intent(activity, AuthActivity::class.java), requestCode)
    }
}