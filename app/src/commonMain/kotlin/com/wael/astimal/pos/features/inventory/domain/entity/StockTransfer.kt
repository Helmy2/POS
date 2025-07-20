import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferDto
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.approved
import pos.app.generated.resources.pending
import pos.app.generated.resources.rejected

data class StockTransfer(
    val id: String,
    val fromStore: Store,
    val toStore: Store,
    val initiatingUser: User,
    val receivingUser: User,
    val status: StockTransferStatus,
    val notes: String?,
    val items: List<StockTransferItem>,
    val isSynced: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class StockTransferStatus {
    PENDING, APPROVED, REJECTED;

    fun getStringResourceId(): StringResource {
        return when (this) {
            PENDING -> Res.string.pending
            APPROVED -> Res.string.approved
            REJECTED -> Res.string.rejected
        }
    }
}

data class StockTransferItem(
    val id: String,
    val product: Product,
    val quantity: Double
)

fun StockTransfer.toDto(): StockTransferDto {
    return StockTransferDto(
        id = id,
        fromStoreId = fromStore.id,
        toStoreId = toStore.id,
        initiatingUserId = initiatingUser.id,
        receivingUserId = receivingUser.id,
        status = status.name,
        notes = notes,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString()
    )
}