package com.tourisain.weijian.presentation.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AboutSoftwareScreenTest {
    @Test
    fun feedbackChannelUsesBothDeveloperEmails() {
        val channel = feedbackEmailChannel()

        assertEquals(listOf("tourisain@163.com", "grllq458@gmail.com"), channel.recipients)
        assertEquals("mailto:tourisain@163.com,grllq458@gmail.com", channel.mailtoUri)
    }

    @Test
    fun feedbackTemplateCarriesVersionAndDeviceContext() {
        val body = feedbackEmailBody(
            baseBody = "Describe here",
            versionName = "2.0.157",
            versionCode = 20157,
            manufacturer = "vivo",
            model = "S60",
            androidVersion = "16"
        )

        assertEquals(
            "Describe here\n\n---\nVersion: 2.0.157 (20157)\nDevice: vivo S60\nAndroid: 16",
            body
        )
    }
}
