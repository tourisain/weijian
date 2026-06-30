package com.tourisain.weijian.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@HiltViewModel
class AppLayoutViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _cardVisibility = MutableStateFlow(
        mapOf(
            "note" to true,
            "account" to true
        )
    )
    val cardVisibility: StateFlow<Map<String, Boolean>> = _cardVisibility.asStateFlow()

    private val _sidebarItems = MutableStateFlow(
        listOf(
            SidebarItem("note_list/default", "笔记", true),
            SidebarItem("accounts", "记账", true),
            SidebarItem("search", "搜索", true),
            SidebarItem("settings", "设置", true)
        )
    )
    val sidebarItems: StateFlow<List<SidebarItem>> = _sidebarItems.asStateFlow()

    init {
        viewModelScope.launch {
            val savedVisibility = userPreferences.getCardVisibility()
            if (savedVisibility.isNotEmpty()) {
                _cardVisibility.value = _cardVisibility.value + savedVisibility.filterKeys { it in supportedCardKeys }
            }
            val saved = userPreferences.getSidebarItems()
            if (saved.isNotEmpty()) {
                _sidebarItems.value = saved.filter { it.key in validRoutes }
            }
        }
    }

    fun toggleCardVisibility(key: String) {
        val next = !_cardVisibility.value.getOrDefault(key, true)
        _cardVisibility.value = _cardVisibility.value + (key to next)
        viewModelScope.launch { userPreferences.saveCardVisibility(key, next) }
    }

    fun toggleSidebarItemVisibility(key: String) {
        val updated = _sidebarItems.value.map {
            if (it.key == key) it.copy(visible = !it.visible) else it
        }
        _sidebarItems.value = updated
        viewModelScope.launch { userPreferences.saveSidebarItems(updated) }
    }

    fun reorderSidebarItems(fromIndex: Int, toIndex: Int) {
        val current = _sidebarItems.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _sidebarItems.value = current
        viewModelScope.launch { userPreferences.saveSidebarItems(current) }
    }
}

@Serializable
data class SidebarItem(val key: String, val name: String, val visible: Boolean)

private val validRoutes = setOf("note_list/default", "accounts", "search", "settings")
private val supportedCardKeys = setOf("note", "account")