package com.tourisain.weijian.presentation.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationInputNormalizerTest {
    @Test
    fun keepsActivationCodeFromMixedText() {
        val input = "激活码：wj5-abcde-fghij-klmno-pqrst-uvwxy-23456-abcde-fghij-klmno"

        assertEquals(
            "WJ5-ABCDE-FGHIJ-KLMNO-PQRST-UVWXY-23456-ABCDE-FGHIJ-KLMNO",
            normalizeActivationCodeInput(input)
        )
    }

    @Test
    fun normalizesCommonDashAndInvisibleCharacters() {
        val input = "wj5－abcde—fghij–klmno‑pqrst\u200Buvwxy 23456"

        assertEquals(
            "WJ5-ABCDE-FGHIJ-KLMNO-PQRSTUVWXY23456",
            normalizeActivationCodeInput(input)
        )
    }
}
