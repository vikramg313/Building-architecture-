package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WholesaleDao {

    @Query("SELECT * FROM suppliers ORDER BY rating DESC, reviewCount DESC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE categoryId = :catId ORDER BY rating DESC")
    fun getSuppliersByCategory(catId: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: String): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE isBookmarked = 1 ORDER BY rating DESC")
    fun getBookmarkedSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun getSupplierCount(): Int

    // Products
    @Query("SELECT * FROM products ORDER BY title ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE supplierId = :supplierId")
    fun getProductsBySupplier(supplierId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :catId")
    fun getProductsByCategory(catId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // RFQs
    @Query("SELECT * FROM rfq_inquiries ORDER BY timestamp DESC")
    fun getAllRfqs(): Flow<List<RfqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRfq(rfq: RfqEntity)

    @Query("UPDATE rfq_inquiries SET status = :status WHERE id = :id")
    suspend fun updateRfqStatus(id: String, status: String)

    @Query("DELETE FROM rfq_inquiries WHERE id = :id")
    suspend fun deleteRfq(id: String)

    // User Account & Settings
    @Query("SELECT * FROM user_account WHERE id = 'active_user' LIMIT 1")
    fun getUserAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserAccount(user: UserAccountEntity)
}
