package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.SmsTemplateEntity

/**
 * Room DAO for SMS Template operations.
 */
@Dao
interface SmsTemplateDao {

    /**
     * Get all SMS templates as a Flow for reactive updates
     */
    @Query("SELECT * FROM sms_template WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllSmsTemplatesFlow(): Flow<List<SmsTemplateEntity>>

    /**
     * Get all SMS templates as a one-time operation
     */
    @Query("SELECT * FROM sms_template WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllSmsTemplates(): List<SmsTemplateEntity>

    /**
     * Get SMS template by ID
     */
    @Query("SELECT * FROM sms_template WHERE _id = :templateId")
    suspend fun getSmsTemplateById(templateId: Long): SmsTemplateEntity?

    /**
     * Get SMS templates by income/expense type
     */
    @Query("SELECT * FROM sms_template WHERE is_income = :isIncome AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getSmsTemplatesByType(isIncome: Boolean): List<SmsTemplateEntity>

    /**
     * Get SMS templates for income
     */
    @Query("SELECT * FROM sms_template WHERE is_income = 1 AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getIncomeSmsTemplates(): List<SmsTemplateEntity>

    /**
     * Get SMS templates for expense
     */
    @Query("SELECT * FROM sms_template WHERE is_income = 0 AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getExpenseSmsTemplates(): List<SmsTemplateEntity>

    /**
     * Insert a new SMS template
     */
    @Insert
    suspend fun insertSmsTemplate(template: SmsTemplateEntity): Long

    /**
     * Update an existing SMS template
     */
    @Update
    suspend fun updateSmsTemplate(template: SmsTemplateEntity)

    /**
     * Delete an SMS template
     */
    @Delete
    suspend fun deleteSmsTemplate(template: SmsTemplateEntity)

    /**
     * Delete SMS template by ID
     */
    @Query("DELETE FROM sms_template WHERE _id = :templateId")
    suspend fun deleteSmsTemplateById(templateId: Long)

    /**
     * Get SMS template count
     */
    @Query("SELECT COUNT(*) FROM sms_template WHERE is_active = 1")
    suspend fun getSmsTemplateCount(): Int

    /**
     * Search SMS templates by title
     */
    @Query("SELECT * FROM sms_template WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchSmsTemplates(query: String): List<SmsTemplateEntity>
}