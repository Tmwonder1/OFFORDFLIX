package com.offordflix.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.offordflix.data.proto.ProfileList
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serializer for ProfileList protobuf data.
 */
object ProfileListSerializer : Serializer<ProfileList> {
    override val defaultValue: ProfileList = ProfileList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProfileList {
        return try {
            ProfileList.parseFrom(input)
        } catch (exception: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: ProfileList, output: OutputStream) {
        t.writeTo(output)
    }
}

/**
 * DataStore extension for ProfileList.
 */
val Context.profileDataStore: DataStore<ProfileList> by dataStore(
    fileName = "user_profiles.pb",
    serializer = ProfileListSerializer
)

/**
 * Data access layer for profile storage using DataStore.
 * 
 * Provides low-level access to profile data persistence with
 * protobuf serialization for efficient storage.
 */
@Singleton
class ProfileDataStore @Inject constructor(
    private val context: Context
) {
    
    /**
     * Access to the profile DataStore.
     */
    val dataStore: DataStore<ProfileList> = context.profileDataStore
    
    /**
     * Generate unique device ID for profile association.
     */
    fun generateDeviceId(): String {
        return "offordflix_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

