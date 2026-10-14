package com.lsp.callguard.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.lsp.callguard.R
import com.lsp.callguard.core.phone.PhoneServices
import com.lsp.callguard.domain.model.DeviceContact
import com.lsp.callguard.domain.phone.PhoneNormalizationResult
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.RegionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val context: Context,
    private val regionProvider: RegionProvider,
    private val normalizer: PhoneNormalizer = PhoneServices.normalizer
) {

    suspend fun getDeviceContacts(): List<DeviceContact> {
        val defaultRegion = regionProvider.defaultRegion()

        return withContext(Dispatchers.IO) {
            val rows = mutableListOf<RawContactRow>()

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
                    rows.add(
                        RawContactRow(
                            id = it.getString(idIndex).orEmpty(),
                            displayName = it.getString(nameIndex).orEmpty(),
                            rawPhone = it.getString(phoneIndex).orEmpty()
                        )
                    )
                }
            }

            mapToDeviceContacts(
                rows = rows,
                normalizer = normalizer,
                defaultRegion = defaultRegion,
                fallbackName = context.getString(R.string.contact_no_name)
            )
        }
    }
}

internal data class RawContactRow(
    val id: String,
    val displayName: String,
    val rawPhone: String
)

/** Keeps only interpretable numbers, deduplicated by canonical E.164 (first row wins). */
internal fun mapToDeviceContacts(
    rows: List<RawContactRow>,
    normalizer: PhoneNormalizer,
    defaultRegion: String?,
    fallbackName: String
): List<DeviceContact> {
    return rows
        .mapNotNull { row ->
            val normalized = normalizer.normalize(row.rawPhone, defaultRegion)
                as? PhoneNormalizationResult.Valid
                ?: return@mapNotNull null

            DeviceContact(
                id = row.id,
                displayName = row.displayName.ifBlank { fallbackName },
                phoneE164 = normalized.phoneE164
            )
        }
        .distinctBy { it.phoneE164 }
}
