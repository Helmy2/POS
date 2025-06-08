package com.wael.astimal.pos.features.client_management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wael.astimal.pos.features.client_management.data.entity.ClientEntity
import com.wael.astimal.pos.features.client_management.data.entity.ClientWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @androidx.room.Transaction
    @Query("SELECT * FROM clients WHERE id = :localId AND isDeletedLocally = 0")
    fun getClientWithDetailsFlow(localId: Long): Flow<ClientWithDetailsEntity?>

    @androidx.room.Transaction
    @Query("SELECT * FROM clients WHERE id = :localId AND isDeletedLocally = 0")
    suspend fun getClientWithDetails(localId: Long): ClientWithDetailsEntity?

    @androidx.room.Transaction
    @Query(
        """
        SELECT c.* FROM clients c INNER JOIN users u ON c.id = u.id
        WHERE c.isDeletedLocally = 0 
        AND (u.arName LIKE '%' || :query || '%' OR u.enName LIKE '%' || :query || '%' OR c.address LIKE '%' || :query || '%')
       
    """
    )
    fun searchClientsWithDetailsFlow(query: String): Flow<List<ClientWithDetailsEntity>>

    @Query("SELECT * FROM clients WHERE isSynced = 0 AND NOT isDeletedLocally")
    suspend fun getUnsyncedCreatedOrUpdatedClients(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE isSynced = 0 AND isDeletedLocally = 1")
    suspend fun getUnsyncedDeletedClients(): List<ClientEntity>

    @Query("DELETE FROM clients WHERE id IN (:localIds)")
    suspend fun deleteClientsByLocalIds(localIds: List<Long>)
}