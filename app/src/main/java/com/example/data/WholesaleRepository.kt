package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.local.RfqEntity
import com.example.data.local.SupplierEntity
import com.example.model.RfqItem
import com.example.model.WholesaleProduct
import com.example.model.WholesaleSupplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WholesaleRepository(private val database: AppDatabase) {

    private val dao = database.wholesaleDao()

    init {
        // Seed initial rich data if empty
        CoroutineScope(Dispatchers.IO).launch {
            if (dao.getSupplierCount() == 0) {
                seedInitialData()
            }
        }
    }

    suspend fun seedInitialData() {
        val supplierEntities = WholesaleSeedData.suppliers.map { SupplierEntity.fromModel(it) }
        val productEntities = WholesaleSeedData.products.map { ProductEntity.fromModel(it) }
        val rfqEntities = WholesaleSeedData.sampleRfqs.map { RfqEntity.fromModel(it) }

        dao.insertSuppliers(supplierEntities)
        dao.insertProducts(productEntities)
        rfqEntities.forEach { dao.insertRfq(it) }
    }

    val allSuppliersWithProducts: Flow<List<WholesaleSupplier>> =
        combine(dao.getAllSuppliers(), dao.getAllProducts()) { suppliers, products ->
            val productMap = products.groupBy { it.supplierId }
            suppliers.map { entity ->
                val supplierProds = productMap[entity.id]?.map { it.toModel() } ?: emptyList()
                entity.toModel(supplierProds)
            }
        }

    val allProducts: Flow<List<WholesaleProduct>> =
        dao.getAllProducts().map { list -> list.map { it.toModel() } }

    val allRfqs: Flow<List<RfqItem>> =
        dao.getAllRfqs().map { list -> list.map { it.toModel() } }

    val bookmarkedSuppliers: Flow<List<WholesaleSupplier>> =
        combine(dao.getBookmarkedSuppliers(), dao.getAllProducts()) { suppliers, products ->
            val productMap = products.groupBy { it.supplierId }
            suppliers.map { entity ->
                val supplierProds = productMap[entity.id]?.map { it.toModel() } ?: emptyList()
                entity.toModel(supplierProds)
            }
        }

    suspend fun toggleBookmark(supplierId: String, currentStatus: Boolean) {
        dao.updateBookmark(supplierId, !currentStatus)
    }

    suspend fun submitRfq(rfq: RfqItem) {
        dao.insertRfq(RfqEntity.fromModel(rfq))
    }

    suspend fun updateRfqStatus(id: String, status: String) {
        dao.updateRfqStatus(id, status)
    }

    suspend fun deleteRfq(id: String) {
        dao.deleteRfq(id)
    }

    suspend fun addNewSupplier(supplier: WholesaleSupplier) {
        dao.insertSupplier(SupplierEntity.fromModel(supplier))
    }

    suspend fun addNewProduct(product: WholesaleProduct) {
        dao.insertProduct(ProductEntity.fromModel(product))
    }

    val userAccountFlow: Flow<com.example.model.UserAccount> =
        dao.getUserAccount().map { entity ->
            entity?.toModel() ?: com.example.model.UserAccount()
        }

    suspend fun saveUserAccount(user: com.example.model.UserAccount) {
        dao.insertOrUpdateUserAccount(com.example.data.local.UserAccountEntity.fromModel(user))
    }
}
