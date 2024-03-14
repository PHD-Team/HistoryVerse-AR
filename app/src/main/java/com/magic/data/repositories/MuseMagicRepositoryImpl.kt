package com.magic.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.magic.data.models.AnchorData
import kotlinx.coroutines.tasks.await

class MuseMagicRepositoryImpl: MuseMagicRepository{
    private val db = FirebaseFirestore.getInstance()
    override suspend  fun getAnchorList(): List<AnchorData> {
        val dataList = mutableListOf<AnchorData>()
        try {
            val documents = db.collection("anchors").get().await()
            for (document in documents) {
                document.toObject(AnchorData::class.java).let {
                    dataList.add(it)
                }
            }
        } catch (e: Exception) {
            Throwable("Error getting documents: ${e.message}")
            Log.w("TAG", "Error getting documents.", e)
        }
        return dataList
    }
}