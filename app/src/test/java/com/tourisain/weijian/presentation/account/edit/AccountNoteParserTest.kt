package com.tourisain.weijian.presentation.account.edit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountNoteParserTest {
    @Test
    fun parsesChineseExpenseIntoStableReadableFields() {
        val result = AccountNoteParser.parse(
            text = "2026年6月27日 午餐 32.5 元，商户：楼下咖啡",
            currentType = "expense",
            currentCategory = "",
            currentAmount = "",
            language = "zh"
        )

        assertEquals("expense", result.type)
        assertEquals(32.5, result.amount ?: 0.0, 0.001)
        assertEquals("CNY", result.currency)
        assertEquals("餐饮", result.category)
        assertEquals("楼下咖啡", result.merchant)
        assertTrue(result.normalizedNote.contains("类型: 支出"))
        assertTrue(result.normalizedNote.contains("分类: 餐饮"))
    }

    @Test
    fun parsesEnglishIncomeWithoutChineseCategoryLeak() {
        val result = AccountNoteParser.parse(
            text = "Salary USD 1200 merchant: Acme Studio",
            currentType = "expense",
            currentCategory = "",
            currentAmount = "",
            language = "en"
        )

        assertEquals("income", result.type)
        assertEquals(1200.0, result.amount ?: 0.0, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Salary", result.category)
        assertEquals("Acme Studio", result.merchant)
        assertTrue(result.normalizedNote.contains("Type: Income"))
    }

    @Test
    fun parsesFrenchExpenseWithLocalizedCategoryAndEuro() {
        val result = AccountNoteParser.parse(
            text = "repas 18,50 euro commercant: Cafe Nord",
            currentType = "expense",
            currentCategory = "",
            currentAmount = "",
            language = "fr"
        )

        assertEquals("expense", result.type)
        assertEquals(18.5, result.amount ?: 0.0, 0.001)
        assertEquals("EUR", result.currency)
        assertEquals("Repas", result.category)
        assertTrue(result.normalizedNote.contains("Type : Depense"))
    }
}
