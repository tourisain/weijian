package com.tourisain.weijian.presentation.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.openExternalUrlStable
import com.tourisain.weijian.presentation.icons.Lucide

internal data class WebsiteItem(
    val title: String,
    val url: String,
    val primary: Boolean = false
)

internal fun officialWebsiteSpecs(): List<WebsiteItem> = listOf(
    WebsiteItem(
        title = "tourisain.cn",
        url = "https://tourisain.cn",
        primary = true
    ),
    WebsiteItem(
        title = "xyster.xyz",
        url = "https://xyster.xyz"
    ),
    WebsiteItem(
        title = "gratia.top",
        url = "https://gratia.top"
    ),
    WebsiteItem(
        title = "aureate.vip",
        url = "https://aureate.vip"
    ),
    WebsiteItem(
        title = "axutongxue.com.cn",
        url = "https://axutongxue.com.cn"
    )
)

@Composable
fun OfficialWebsitesScreen(navController: NavController) {
    val context = LocalContext.current
    val websites = officialWebsiteSpecs()

    SimpleSettingsPage(navController, stringResource(R.string.official_websites)) {
        SettingsGroup {
            websites.forEachIndexed { index, website ->
                SettingItem(
                    icon = Lucide.Globe,
                    title = website.title,
                    subtitle = stringResource(
                        if (website.primary) R.string.official_website_primary else R.string.official_website_developer_site
                    ),
                    onClick = { openWebsite(context, website.url) }
                )
                if (index != websites.lastIndex) SettingsDivider()
            }
        }
    }
}

private fun openWebsite(context: Context, url: String) {
    context.openExternalUrlStable(url)
}
