package com.magic.data.repositories

import com.magic.data.models.AnchorData

interface MuseMagicRepository {
    suspend fun getAnchorList(): List<AnchorData>
}