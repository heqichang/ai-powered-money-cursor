package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.data.local.entity.CategoryType
import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject

class ImportLedgerDataUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(jsonString: String): Long {
        val json = JSONObject(jsonString)
        
        // 解析账本
        val ledgerJson = json.getJSONObject("ledger")
        val ledger = Ledger(
            id = 0, // 导入时创建新账本，ID由数据库自动生成
            name = ledgerJson.getString("name"),
            description = ledgerJson.getString("description"),
            createdAtEpochMillis = ledgerJson.optLong("createdAtEpochMillis", System.currentTimeMillis())
        )
        
        // 保存账本
        ledgerRepository.upsert(ledger)
        
        // 等待账本保存完成，获取新ID
        // 使用重试机制等待账本出现在列表中
        var savedLedger: Ledger? = null
        repeat(10) { // 最多重试10次
            val ledgers = ledgerRepository.observeLedgers().first()
            savedLedger = ledgers.find { 
                it.name == ledger.name && it.description == ledger.description 
            }
            if (savedLedger != null) return@repeat
            delay(100) // 等待100ms后重试
        }
        
        val newLedgerId = savedLedger?.id ?: throw IllegalStateException("无法找到保存的账本")
        
        // 解析并保存类别
        val categoriesJson = json.getJSONArray("categories")
        val categoryIdMap = mutableMapOf<Long, Long>() // 旧ID -> 新ID
        
        for (i in 0 until categoriesJson.length()) {
            val categoryJson = categoriesJson.getJSONObject(i)
            val oldCategoryId = categoryJson.getLong("id")
            val category = Category(
                id = 0, // 创建新类别
                ledgerId = newLedgerId,
                name = categoryJson.getString("name"),
                type = CategoryType.valueOf(categoryJson.getString("type")),
                colorHex = categoryJson.optString("colorHex").takeIf { it.isNotEmpty() && it != "null" },
                iconName = categoryJson.optString("iconName").takeIf { it.isNotEmpty() && it != "null" },
                isDefault = categoryJson.optBoolean("isDefault", false)
            )
            categoryRepository.upsert(category)
            
            // 等待类别保存完成，获取新ID
            var savedCategory: Category? = null
            repeat(10) { // 最多重试10次
                val categories = categoryRepository.observeCategories(newLedgerId).first()
                savedCategory = categories.find { 
                    it.name == category.name && it.ledgerId == newLedgerId 
                }
                if (savedCategory != null) return@repeat
                delay(100) // 等待100ms后重试
            }
            
            savedCategory?.let {
                categoryIdMap[oldCategoryId] = it.id
            }
        }
        
        // 解析并保存交易
        val transactionsJson = json.getJSONArray("transactions")
        for (i in 0 until transactionsJson.length()) {
            val transactionJson = transactionsJson.getJSONObject(i)
            val oldCategoryId = transactionJson.optLong("categoryId", -1)
            val newCategoryId = if (oldCategoryId > 0) categoryIdMap[oldCategoryId] else null
            
            val transaction = MoneyTransaction(
                id = 0, // 创建新交易
                ledgerId = newLedgerId,
                categoryId = newCategoryId,
                amountInCents = transactionJson.getLong("amountInCents"),
                occurredOn = LocalDate.parse(transactionJson.getString("occurredOn")),
                note = transactionJson.optString("note").takeIf { it.isNotEmpty() && it != "null" },
                createdAtEpochMillis = transactionJson.optLong("createdAtEpochMillis", System.currentTimeMillis())
            )
            transactionRepository.upsert(transaction)
        }
        
        return newLedgerId
    }
}

