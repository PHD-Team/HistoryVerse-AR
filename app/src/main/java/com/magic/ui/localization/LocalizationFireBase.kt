package com.magic.ui.localization

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.magic.ui.localization.Constants.ORDER
import com.magic.ui.localization.Constants.PATHS
import com.magic.ui.localization.models.FireBasePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocalizationFireBase {

    private val firestore : FirebaseFirestore = Firebase.firestore

    suspend fun getPathAnchorsIds(order : Int ) : FireBasePath {
        var path : FireBasePath? = FireBasePath()
        try {
            val document = firestore.collection(PATHS).whereEqualTo(ORDER , order).get()
                .await().documents.first()
            path = document.toObject<FireBasePath>()
        } catch (e : Exception) {

        }
        return path !!
    }
}