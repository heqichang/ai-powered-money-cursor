package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.json.JSONArray
import javax.inject.Inject

class ExportLedgerDataUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(ledgerId: Long): String {
        val ledger = ledgerRepository.observeLedger(ledgerId).first() ?: throw IllegalStateException("账本不存在")
        val categories = categoryRepository.observeCategories(ledgerId).first()
        val transactions = transactionRepository.getAllTransactions(ledgerId)
        
        val json = JSONObject().apply {
            put("version", 1)
            put("ledger", ledgerToJson(ledger))
            put("categories", categoriesToJson(categories))
            put("transactions", transactionsToJson(transactions))
        }
        
        return json.toString(2) // 格式化输出，缩进2个空格
    }
    
    private fun ledgerToJson(ledger: Ledger): JSONObject {
        return JSONObject().apply {
            put("id", ledger.id)
            put("name", ledger.name)
            put("description", ledger.description)
            put("createdAtEpochMillis", ledger.createdAtEpochMillis)
        }
    }
    
    private fun categoriesToJson(categories: List<Category>): JSONArray {
        return JSONArray().apply {
            categories.forEach { category ->
                put(JSONObject().apply {
                    put("id", category.id)
                    put("ledgerId", category.ledgerId)
                    put("name", category.name)
                    put("type", category.type.name)
                    put("colorHex", category.colorHex)
                    put("iconName", category.iconName)
                    put("isDefault", category.isDefault)
                })
            }
        }
    }
    
    private fun transactionsToJson(transactions: List<MoneyTransaction>): JSONArray {
        return JSONArray().apply {
            transactions.forEach { transaction ->
                put(JSONObject().apply {
                    put("id", transaction.id)
                    put("ledgerId", transaction.ledgerId)
                    put("categoryId", transaction.categoryId)
                    put("amountInCents", transaction.amountInCents)
                    put("occurredOn", transaction.occurredOn.toString())
                    put("note", transaction.note)
                    put("createdAtEpochMillis", transaction.createdAtEpochMillis)
                })
            }
        }
    }
}

