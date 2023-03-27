package com.app.receiptscanner.storage

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.parser.FieldTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets


/**
 * A class encompassing all storage related actions for the application. All actions involved are
 * done through AndroidX Jetpack Security to ensure that the sensitive information stored cannot be
 * read by a third party.
 *
 * @see EncryptedFile
 * @see MasterKey
 */
class StorageHandler(private val applicationContext: Context) {
    /**
     * Accesses an encrypted file in internal storage
     *
     * @param storageDirectory the file path for the application's internal storage
     * @param filename the name of the file to be found
     * @return an EncryptedFile which can be used to read from or write to
     * @see EncryptedFile
     */
    private fun getEncryptedFile(storageDirectory: String, filename: String): EncryptedFile {
        val directory = File(storageDirectory + RECEIPT_SUB_PATH)
        if (!directory.exists()) directory.mkdirs()

        val masterKey = MasterKey
            .Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val file = File(directory, filename)

        return EncryptedFile.Builder(
            applicationContext,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    /**
     * Fetches a given receipt from internal storage. This receipt will have no information on the
     * specifics for it's type as it has been standardized
     *
     * @param receipt the Receipt to be fetched from external storage
     * @param storageDirectory the file path for the application's internal storage
     * @return a NormalizedReceipt containing all the fields for the stored receipt
     */
    suspend fun readReceipt(receipt: Receipt, storageDirectory: String) = withContext(Dispatchers.IO){
        val encryptedFile = getEncryptedFile(storageDirectory, "id-${receipt.id}")
        val byteArrayOutputStream = ByteArrayOutputStream()
        encryptedFile.openFileInput().use { fileStream ->
            var bytesRead = fileStream.read()
            while (bytesRead != -1) {
                byteArrayOutputStream.write(bytesRead)
                bytesRead = fileStream.read()
            }
        }
        val data = byteArrayOutputStream.toByteArray()
        val json = JSONObject(data.toString(StandardCharsets.UTF_8))
        val name: String = json[FIELD_NAME].toString()
        val type: Int = json[FIELD_TYPE].toString().toInt()
        val fields = FieldTemplate.getFieldsById(type)
        json.keys().forEach {
            if (it != FIELD_NAME) {
                val identifier = ArrayList(it.split(SEPARATOR))
                val fieldData = ArrayList(json[it].toString().split(SEPARATOR))
                fields.set(identifier, fieldData)
            }
        }
        return@withContext NormalizedReceipt(name, receipt.dateCreated, receipt.photoPath, type, fields)
    }

    /**
     * Stores a given receipt in internal storage. The normalized version of the receipt is first
     * converted to JSON before being encrypted and stored.
     *
     * @param receipt the Receipt to be fetched from external storage
     * @param normalizedReceipt a standardized version of the receipt to be stored
     * @param storageDirectory the file path for the application's internal storage
     */
    suspend fun storeReceipt(
        receipt: Receipt,
        normalizedReceipt: NormalizedReceipt,
        storageDirectory: String
    ) = withContext(Dispatchers.IO) {
        val encryptedFile = getEncryptedFile(storageDirectory, "id-${receipt.id}")
        val json = JSONObject()
        json.put(FIELD_NAME, normalizedReceipt.name)
        json.put(FIELD_TYPE, normalizedReceipt.type)
        normalizedReceipt.fields.getMap().forEach {
            val key = it.key.joinToString(SEPARATOR)
            json.put(key, it.value.data.joinToString(SEPARATOR))
        }
        val data = json.toString().toByteArray()
        encryptedFile.openFileOutput().use {
            it.write(data)
        }
    }

    companion object {
        private const val SEPARATOR = ":"
        private const val FIELD_NAME = "Name"
        private const val FIELD_TYPE = "Type"
        private const val RECEIPT_SUB_PATH = "/receipts/"
    }
}