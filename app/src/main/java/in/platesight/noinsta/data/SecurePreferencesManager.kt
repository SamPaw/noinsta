package `in`.platesight.noinsta.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "noinsta_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var deviceId: String?
        get() = securePrefs.getString(KEY_DEVICE_ID, null)
        set(value) = securePrefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var accessToken: String?
        get() = securePrefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = securePrefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = securePrefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = securePrefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var userId: String?
        get() = securePrefs.getString(KEY_USER_ID, null)
        set(value) = securePrefs.edit().putString(KEY_USER_ID, value).apply()

    fun clear() {
        securePrefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }
}
