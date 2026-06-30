package com.tourisain.weijian.presentation.account.edit

import java.util.Locale

data class AccountNoteParseResult(
    val amount: Double?,
    val currency: String,
    val type: String,
    val category: String?,
    val merchant: String?,
    val datetime: String?,
    val normalizedNote: String
)

object AccountNoteParser {
    fun parse(
        text: String,
        currentType: String,
        currentCategory: String,
        currentAmount: String,
        language: String = Locale.getDefault().language,
        categorize: Boolean = true
    ): AccountNoteParseResult {
        val amountPair = parseAmountAndCurrency(text)
        val amount = amountPair.first ?: currentAmount.toDoubleOrNull()
        val currency = amountPair.second ?: detectCurrency(text)
        val type = detectRecordType(text) ?: currentType
        val category = if (categorize) {
            detectCategory(text, language) ?: currentCategory.takeIf { it.isNotBlank() }
        } else {
            currentCategory.takeIf { it.isNotBlank() }
        }
        val merchant = parseMerchant(text)
        val datetime = parseDatetimeString(text)
        val normalized = buildReadableNote(
            amount = amount,
            currency = currency,
            type = type,
            category = category,
            merchant = merchant,
            datetime = datetime,
            raw = text,
            language = language
        )

        return AccountNoteParseResult(
            amount = amount,
            currency = currency,
            type = type,
            category = category,
            merchant = merchant,
            datetime = datetime,
            normalizedNote = normalized
        )
    }

    private fun detectRecordType(text: String): String? {
        val lower = text.lowercase(Locale.ROOT)
        val incomeKeys = listOf(
            "收入", "工资", "奖金", "投资", "兼职", "转入", "收款", "退款", "利息", "红包", "理财", "报销",
            "salary", "income", "bonus", "refund", "reimbursement", "interest", "dividend",
            "salaire", "revenu", "prime", "remboursement", "interet"
        )
        val expenseKeys = listOf(
            "支出", "消费", "花费", "开销", "午餐", "早餐", "晚餐", "交通", "购物", "房租", "水电", "支付", "买单",
            "expense", "spent", "paid", "payment", "lunch", "dinner", "rent", "shopping",
            "depense", "paye", "paiement", "loyer", "achats", "repas"
        )
        val isIncome = incomeKeys.any { lower.contains(it.lowercase(Locale.ROOT)) }
        val isExpense = expenseKeys.any { lower.contains(it.lowercase(Locale.ROOT)) }
        return when {
            isIncome && !isExpense -> "income"
            isExpense && !isIncome -> "expense"
            else -> null
        }
    }

    private fun detectCategory(text: String, language: String): String? {
        val lower = text.lowercase(Locale.ROOT)
        return categoryRules.firstOrNull { rule ->
            rule.keys.any { lower.contains(it.lowercase(Locale.ROOT)) }
        }?.labelFor(language)
    }

    private fun parseAmountAndCurrency(text: String): Pair<Double?, String?> {
        for (pattern in amountPatterns) {
            val match = pattern.regex.find(text) ?: continue
            val amount = match.groupValues.getOrNull(pattern.amountGroup)
                ?.let(::normalizeAmount)
                ?.toDoubleOrNull()
            if (amount != null && amount > 0) {
                return amount to pattern.currency
            }
        }
        return extractAmount(text) to null
    }

    private fun extractAmount(text: String): Double? {
        return Regex("""\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:[.,]\d{1,2})?""")
            .findAll(text)
            .mapNotNull { normalizeAmount(it.value).toDoubleOrNull() }
            .firstOrNull { it > 0 }
    }

    private fun normalizeAmount(raw: String): String {
        val trimmed = raw.trim()
        return when {
            "," in trimmed && "." in trimmed -> trimmed.replace(",", "")
            "," in trimmed -> trimmed.replace(",", ".")
            else -> trimmed
        }
    }

    private fun detectCurrency(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("usd") || lower.contains("dollar") || text.contains("$") -> "USD"
            lower.contains("eur") || lower.contains("euro") || text.contains("€") -> "EUR"
            else -> "CNY"
        }
    }

    private fun parseDatetimeString(text: String): String? {
        val patterns = listOf(
            Regex("""\d{4}[-/]\d{1,2}[-/]\d{1,2}(?:[ T]\d{1,2}:\d{2})?"""),
            Regex("""\d{4}年\d{1,2}月\d{1,2}日(?:\s*\d{1,2}:\d{2})?"""),
            Regex("""\d{1,2}:\d{2}""")
        )
        return patterns.firstNotNullOfOrNull { it.find(text)?.value }
    }

    private fun parseMerchant(text: String): String? {
        val patterns = listOf(
            Regex("""(?:商户|商家|付款给|merchant|store|payee|commercant)[:：\s]+(.+?)(?:[，,。\n]|$)""", RegexOption.IGNORE_CASE)
        )
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }

    private fun buildReadableNote(
        amount: Double?,
        currency: String,
        type: String,
        category: String?,
        merchant: String?,
        datetime: String?,
        raw: String,
        language: String
    ): String {
        val copy = labelsFor(language, type)
        return buildString {
            append(copy.source).append(": AI\n")
            if (amount != null) {
                append(copy.amount).append(": ")
                    .append(String.format(Locale.ROOT, "%.2f", amount))
                    .append(' ')
                    .append(currency)
                    .append('\n')
            }
            append(copy.type).append(": ").append(copy.typeValue).append('\n')
            if (category != null) append(copy.category).append(": ").append(category).append('\n')
            if (merchant != null) append(copy.merchant).append(": ").append(merchant).append('\n')
            if (datetime != null) append(copy.time).append(": ").append(datetime).append('\n')
            append(copy.raw).append(": ").append(raw.trim())
        }
    }

    private fun labelsFor(language: String, type: String): NoteLabels {
        return when (language) {
            "en" -> NoteLabels("Source", "Amount", "Type", if (type == "income") "Income" else "Expense", "Category", "Merchant", "Time", "Original")
            "fr" -> NoteLabels("Source", "Montant", "Type ", if (type == "income") "Revenu" else "Depense", "Categorie", "Commercant", "Temps", "Texte")
            else -> NoteLabels("来源", "金额", "类型", if (type == "income") "收入" else "支出", "分类", "商家", "时间", "原文")
        }
    }

    private data class NoteLabels(
        val source: String,
        val amount: String,
        val type: String,
        val typeValue: String,
        val category: String,
        val merchant: String,
        val time: String,
        val raw: String
    )

    private data class AmountPattern(
        val regex: Regex,
        val amountGroup: Int,
        val currency: String
    )

    private data class CategoryRule(
        val zh: String,
        val en: String,
        val fr: String,
        val keys: List<String>
    ) {
        fun labelFor(language: String): String = when (language) {
            "en" -> en
            "fr" -> fr
            else -> zh
        }
    }

    private val amountPatterns = listOf(
        AmountPattern(Regex("""[¥￥]\s*(\d[\d,.]*)"""), 1, "CNY"),
        AmountPattern(Regex("""\$\s*(\d[\d,.]*)"""), 1, "USD"),
        AmountPattern(Regex("""€\s*(\d[\d,.]*)"""), 1, "EUR"),
        AmountPattern(Regex("""(\d[\d,.]*)\s*(元|块|rmb|cny|人民币)""", RegexOption.IGNORE_CASE), 1, "CNY"),
        AmountPattern(Regex("""(\d[\d,.]*)\s*(usd|dollar|dollars|美元)""", RegexOption.IGNORE_CASE), 1, "USD"),
        AmountPattern(Regex("""(usd|dollar|dollars|美元)\s*(\d[\d,.]*)""", RegexOption.IGNORE_CASE), 2, "USD"),
        AmountPattern(Regex("""(\d[\d,.]*)\s*(eur|euro|euros)""", RegexOption.IGNORE_CASE), 1, "EUR"),
        AmountPattern(Regex("""(eur|euro|euros)\s*(\d[\d,.]*)""", RegexOption.IGNORE_CASE), 2, "EUR")
    )

    private val categoryRules = listOf(
        CategoryRule("餐饮", "Food", "Repas", listOf("餐饮", "吃饭", "早餐", "午餐", "晚餐", "奶茶", "咖啡", "外卖", "food", "meal", "lunch", "dinner", "coffee", "repas", "cafe")),
        CategoryRule("交通", "Transport", "Transport", listOf("交通", "打车", "地铁", "公交", "出租车", "加油", "停车", "train", "taxi", "bus", "metro", "fuel", "transport")),
        CategoryRule("购物", "Shopping", "Achats", listOf("购物", "买菜", "衣服", "超市", "商场", "淘宝", "京东", "shopping", "market", "store", "achats", "magasin")),
        CategoryRule("娱乐", "Entertainment", "Loisirs", listOf("娱乐", "电影", "游戏", "会员", "视频", "音乐", "旅游", "movie", "game", "music", "travel", "loisirs")),
        CategoryRule("居住", "Housing", "Logement", listOf("居住", "房租", "租金", "水电", "物业", "电费", "水费", "rent", "housing", "utilities", "loyer", "logement")),
        CategoryRule("医疗", "Healthcare", "Sante", listOf("医疗", "医院", "药费", "体检", "挂号", "药品", "hospital", "medicine", "doctor", "sante")),
        CategoryRule("教育", "Education", "Education", listOf("教育", "学费", "培训", "课程", "书费", "考试", "education", "course", "book", "school", "ecole")),
        CategoryRule("工资", "Salary", "Salaire", listOf("工资", "薪资", "薪水", "月薪", "salary", "salaire")),
        CategoryRule("奖金", "Bonus", "Prime", listOf("奖金", "提成", "分红", "bonus", "prime")),
        CategoryRule("投资", "Investment", "Investissement", listOf("投资", "理财", "基金", "股票", "investment", "dividend", "fund", "investissement"))
    )
}
