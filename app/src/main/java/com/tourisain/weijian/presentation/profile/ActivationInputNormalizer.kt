package com.tourisain.weijian.presentation.profile

import java.util.Locale

private val ActivationCodePattern = Regex("WJ5(?:-[A-Z2-7]{1,5}){8,90}", RegexOption.IGNORE_CASE)
private val DashLikeCharacters = Regex("[\\u2010\\u2011\\u2012\\u2013\\u2014\\u2015\\u2212\\uFE58\\uFE63\\uFF0D]")
private val InvisibleCharacters = Regex("[\\u200B\\u200C\\u200D\\u2060\\uFEFF]")

internal fun normalizeActivationCodeInput(value: String): String {
    val normalizedSeparators = value
        .replace(DashLikeCharacters, "-")
        .replace(InvisibleCharacters, "")
    val compact = normalizedSeparators.replace(Regex("\\s+"), "")
    val candidate = ActivationCodePattern.find(compact)?.value ?: compact
    return candidate.uppercase(Locale.US).replace(Regex("[^A-Z0-9-]"), "")
}
