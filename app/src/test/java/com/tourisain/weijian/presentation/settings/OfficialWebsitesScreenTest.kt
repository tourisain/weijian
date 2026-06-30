package com.tourisain.weijian.presentation.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class OfficialWebsitesScreenTest {
    @Test
    fun officialWebsiteListIncludesAllDeveloperSites() {
        val sites = officialWebsiteSpecs()

        assertEquals(
            listOf("tourisain.cn", "xyster.xyz", "gratia.top", "aureate.vip", "axutongxue.com.cn"),
            sites.map { it.title }
        )
        assertEquals(
            listOf(
                "https://tourisain.cn",
                "https://xyster.xyz",
                "https://gratia.top",
                "https://aureate.vip",
                "https://axutongxue.com.cn"
            ),
            sites.map { it.url }
        )
    }
}
