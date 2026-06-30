package com.tourisain.weijian.presentation.account.edit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.CategoryEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountEditViewModel @Inject constructor(
    private val repository: AccountRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val recordId: String? = savedStateHandle["recordId"]

    private val _amount = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _type = MutableStateFlow("expense") // expense or income
    val type = _type.asStateFlow()

    private val _category = MutableStateFlow("")
    val category = _category.asStateFlow()

    private val _note = MutableStateFlow("")
    val note = _note.asStateFlow()

    private val _record = MutableStateFlow<AccountRecordEntity?>(null)

    private val _autoEnabled = MutableStateFlow(false)
    val autoEnabled = _autoEnabled.asStateFlow()

    private val _autoFrequency = MutableStateFlow("none")
    val autoFrequency = _autoFrequency.asStateFlow()

    private val _imageUri = MutableStateFlow<String?>(null)
    val imageUri = _imageUri.asStateFlow()

    val smartCategorizationEnabled = userPreferences.enableSmartCategorization
        .catch { emit(true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val smartSuggestionsEnabled = userPreferences.enableSmartSuggestions
        .catch { emit(true) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isUserPro = userRepository.currentUserState.map { user ->
        user?.isPro == true && (user.proExpireDate == null || user.proExpireDate > System.currentTimeMillis())
    }.catch {
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val categories = _type.flatMapLatest { currentType: String ->
        userRepository.currentUserId.flatMapLatest { userId: String ->
            repository.getCategoriesByType(userId, currentType).combine(isUserPro) { dbCategories: List<CategoryEntity>, isPro: Boolean ->
                val defaults = if (currentType == "expense") defaultExpenseCategories() else defaultIncomeCategories()

                val dbNames = dbCategories.map { it.name }.toSet()
                val newDefaults = defaults.filter { !dbNames.contains(it) }.map {
                    CategoryEntity(id = UUID.randomUUID().toString(), userId = userId, name = it, type = currentType)
                }

                if (isPro) {
                    newDefaults + dbCategories
                } else {
                    newDefaults
                }
            }
        }
    }.catch {
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showAddCategoryDialog = MutableStateFlow(false)
    val showAddCategoryDialog = _showAddCategoryDialog.asStateFlow()

    private val _showProDialog = MutableStateFlow(false)
    val showProDialog = _showProDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        if (recordId != null && recordId != "new") {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val userId = userRepository.currentUserId.first()
                    repository.getRecordById(recordId, userId)?.let {
                        _record.value = it
                        _amount.value = it.amount.toString()
                        _type.value = it.type
                        _category.value = it.category
                        _note.value = it.note
                        _imageUri.value = it.imageUri
                        lastPersistedDraft = createDraftSignature(it.amount)
                    }
                } catch (e: Exception) {
                    _error.value = context.getString(R.string.account_load_failed, e.message.orEmpty())
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _amount.value = value
            scheduleAutoSave()
        }
    }

    fun onTypeChange(value: String) {
        _type.value = value
        scheduleAutoSave()
    }

    fun onCategoryChange(value: String) {
        _category.value = value
        scheduleAutoSave()
    }

    fun onNoteChange(value: String) {
        _note.value = value
        scheduleAutoSave()
    }

    fun onAutoEnabledChange(enabled: Boolean) {
        _autoEnabled.value = enabled
        if (enabled) {
            scheduleAutoSave()
        } else {
            autoSaveJob?.cancel()
        }
    }

    fun onAutoFrequencyChange(value: String) {
        _autoFrequency.value = value
    }

    fun setImageUri(uri: String?) {
        _imageUri.value = uri
        scheduleAutoSave()
    }

    fun clearImage() {
        _imageUri.value = null
        scheduleAutoSave()
    }

    fun applyAiFromNote() {
        val text = _note.value
        if (text.isBlank()) return

        val result = AccountNoteParser.parse(
            text = text,
            currentType = _type.value,
            currentCategory = _category.value,
            currentAmount = _amount.value,
            language = Locale.getDefault().language,
            categorize = smartCategorizationEnabled.value
        )

        _type.value = result.type
        result.category?.takeIf { it.isNotBlank() }?.let { _category.value = it }
        result.amount?.let { _amount.value = it.toString() }
        _note.value = result.normalizedNote
    }

    private fun defaultExpenseCategories(): List<String> = when (Locale.getDefault().language) {
        "en" -> listOf("Food", "Transport", "Shopping", "Entertainment", "Housing", "Healthcare", "Education", "Other")
        "fr" -> listOf("Repas", "Transport", "Achats", "Loisirs", "Logement", "Sante", "Education", "Autre")
        else -> listOf("餐饮", "交通", "购物", "娱乐", "居住", "医疗", "教育", "其他")
    }

    private fun defaultIncomeCategories(): List<String> = when (Locale.getDefault().language) {
        "en" -> listOf("Salary", "Bonus", "Investment", "Part-time", "Other")
        "fr" -> listOf("Salaire", "Prime", "Investissement", "Temps partiel", "Autre")
        else -> listOf("工资", "奖金", "投资", "兼职", "其他")
    }

    fun onAddCategoryClick() {
        if (isUserPro.value) {
            _showAddCategoryDialog.value = true
        } else {
            _showProDialog.value = true
        }
    }

    fun dismissAddCategoryDialog() {
        _showAddCategoryDialog.value = false
    }

    fun dismissProDialog() {
        _showProDialog.value = false
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        if (!isUserPro.value) {
            _error.value = context.getString(R.string.custom_category_premium_message)
            return
        }
        viewModelScope.launch {
            val userId = userRepository.currentUserId.first()
            try {
                repository.insertCategory(CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = name,
                    type = _type.value
                ))
                _category.value = name
                _showAddCategoryDialog.value = false
            } catch (e: Exception) {
                _error.value = context.getString(R.string.account_add_category_failed, e.message.orEmpty())
            }
        }
    }

    fun saveRecord(onSuccess: () -> Unit) {
        autoSaveJob?.cancel()
        autoSaveJob = null
        val amountVal = _amount.value.toDoubleOrNull()
        if (amountVal == null) {
            _error.value = context.getString(R.string.account_amount_invalid)
            return
        }
        if (_category.value.isBlank()) {
            _error.value = context.getString(R.string.account_category_required)
            return
        }
        if (_isLoading.value) return

        viewModelScope.launch {
            saveMutex.withLock {
                _isLoading.value = true
                try {
                    val userId = userRepository.currentUserId.first()
                    val currentRecord = _record.value
                    val now = System.currentTimeMillis()
                    if (currentRecord != null) {
                        val draft = createDraftSignature(amountVal)
                        if (draft == lastPersistedDraft) {
                            onSuccess()
                            return@withLock
                        }
                        val updatedRecord = currentRecord.copy(
                            amount = amountVal,
                            type = _type.value,
                            category = _category.value,
                            note = _note.value,
                            createdAt = currentRecord.createdAt,
                            imageUri = _imageUri.value
                        )
                        repository.updateRecord(updatedRecord)
                        _record.value = updatedRecord
                        lastPersistedDraft = draft
                    } else {
                        if (!canCreateAdditionalRecord(userId)) {
                            _error.value = context.getString(R.string.account_member_limit_message)
                            return@withLock
                        }
                        val newRecord = AccountRecordEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            amount = amountVal,
                            type = _type.value,
                            category = _category.value,
                            note = _note.value,
                            imageUri = _imageUri.value,
                            date = now,
                            createdAt = now
                        )
                        repository.insertRecord(newRecord)
                        _record.value = newRecord
                        lastPersistedDraft = createDraftSignature(amountVal)
                    }
                    onSuccess()
                } catch (e: Exception) {
                    _error.value = context.getString(R.string.account_save_failed, e.message.orEmpty())
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private var autoSaveJob: Job? = null
    private val saveMutex = Mutex()
    private var lastPersistedDraft: AccountRecordDraftSignature? = null
    private fun scheduleAutoSave() {
        if (!_autoEnabled.value) return
        val amountVal = _amount.value.toDoubleOrNull()
        if (_record.value == null && (amountVal == null || _category.value.isBlank())) return
        
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500)
            saveMutex.withLock {
                try {
                    val userId = userRepository.currentUserId.first()
                    val currentRecord = _record.value
                    val now = System.currentTimeMillis()
                    if (currentRecord != null) {
                        val draft = createDraftSignature(amountVal ?: currentRecord.amount)
                        if (draft == lastPersistedDraft) return@withLock
                        val updatedRecord = currentRecord.copy(
                            amount = amountVal ?: currentRecord.amount,
                            type = _type.value,
                            category = _category.value.let { if (it.isNotBlank()) it else currentRecord.category },
                            note = _note.value,
                            createdAt = currentRecord.createdAt,
                            imageUri = _imageUri.value
                        )
                        repository.updateRecord(updatedRecord)
                        _record.value = updatedRecord
                        lastPersistedDraft = draft
                    } else if (amountVal != null && _category.value.isNotBlank()) {
                        if (!canCreateAdditionalRecord(userId)) return@withLock
                        val newRecord = AccountRecordEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            amount = amountVal,
                            type = _type.value,
                            category = _category.value,
                            note = _note.value,
                            imageUri = _imageUri.value,
                            date = now,
                            createdAt = now
                        )
                        repository.insertRecord(newRecord)
                        _record.value = newRecord
                        lastPersistedDraft = createDraftSignature(amountVal)
                    }
                } catch (e: Exception) {
                    // Ignore auto-save error
                }
            }
        }
    }

    private suspend fun canCreateAdditionalRecord(userId: String): Boolean {
        if (userRepository.isUserPro(userId)) return true
        return repository.getRecordCount(userId) < FREE_ACCOUNT_RECORD_LIMIT
    }

    fun clearError() {
        _error.value = null
    }

    private fun createDraftSignature(amount: Double): AccountRecordDraftSignature {
        return AccountRecordDraftSignature(
            amount = amount,
            type = _type.value,
            category = _category.value,
            noteLength = _note.value.length,
            noteHash = _note.value.hashCode(),
            imageUri = _imageUri.value
        )
    }

    private companion object {
        const val FREE_ACCOUNT_RECORD_LIMIT = 3
    }

    override fun onCleared() {
        autoSaveJob?.cancel()
        super.onCleared()
    }
}

private data class AccountRecordDraftSignature(
    val amount: Double,
    val type: String,
    val category: String,
    val noteLength: Int,
    val noteHash: Int,
    val imageUri: String?
)
