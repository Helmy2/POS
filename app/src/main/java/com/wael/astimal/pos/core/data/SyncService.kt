package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.flow.first


class SyncServiceImpl(
    private val syncApiService: SyncApiService,
    private val syncManager: SyncManager,
    private val unitRepository: UnitRepository
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            val lastSyncDate = syncManager.getLastSyncDate().first()
            val syncRequest = SyncRequest(lastSyncDate = lastSyncDate)

            val unitResult = syncApiService.syncUnits(syncRequest)

            unitResult.onSuccess { response ->
                unitRepository.syncWithServer(response.data.units)
                // IMPORTANT: Update the sync date only after a successful sync
                syncManager.updateLastSyncDate(response.data.nextSyncDate)
            }.onFailure {
                // If one sync fails, we stop the entire process
                return Result.failure(it)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
}
