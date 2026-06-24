package com.lsp.callguard.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.lsp.callguard.domain.model.DeviceContact
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val context: Context
) {

    suspend fun getDeviceContacts(): List<DeviceContact> {
        return withContext(Dispatchers.IO) {
            val contacts = mutableListOf<DeviceContact>()

            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                )
                val nameIndex = it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val phoneIndex = it.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                while (it.moveToNext()) {
                    val contactId = it.getString(idIndex).orEmpty()
                    val displayName = it.getString(nameIndex).orEmpty()
                    val rawPhone = it.getString(phoneIndex).orEmpty()

                    val normalized = PhoneNormalizer.normalize(rawPhone)

                    if (normalized is PhoneNormalizationResult.Valid) {
                        contacts.add(
                            DeviceContact(
                                id = contactId,
                                displayName = displayName.ifBlank { "Sem nome" },
                                phoneE164 = normalized.phoneE164
                            )
                        )
                    }
                }
            }

            contacts
                .distinctBy { it.phoneE164 }
        }
    }
}