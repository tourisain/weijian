package com.tourisain.weijian.presentation.note.detail

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.NoteCategoryRepository
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.presentation.note.isVisibleStandaloneNote
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.ArrayDeque
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val noteCategoryRepository: NoteCategoryRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val noteId: String? = savedStateHandle["noteId"]
    private val categoryIdFromRoute: String? = savedStateHandle["categoryId"]

    private val _note = MutableStateFlow<NoteEntity?>(null)
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()
    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()
    private val _isPinned = MutableStateFlow(false)
    val isPinned = _isPinned.asStateFlow()
    private val _isArchived = MutableStateFlow(false)
    val isArchived = _isArchived.asStateFlow()
    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()
    private val _format = MutableStateFlow("plain")
    val format = _format.asStateFlow()
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags = _tags.asStateFlow()
    private val _type = MutableStateFlow("text")
    val type = _type.asStateFlow()
    private val _attachments = MutableStateFlow<List<String>>(emptyList())
    val attachments = _attachments.asStateFlow()
    private val _categoryId = MutableStateFlow<String?>(categoryIdFromRoute?.takeUnless { it == "all" || it == "default" || it == "null" || it == "uncategorized" })
    val categoryId = _categoryId.asStateFlow()
    val folderOptions = userRepository.currentUserId
        .flatMapLatest { userId -> noteCategoryRepository.getAllCategories(userId) }
        .map { folders ->
            folders
                .sortedWith(compareBy<NoteCategoryEntity> { it.sortOrder }.thenBy { it.createdAt })
                .map { NoteFolderOption(id = it.id, name = it.name, color = it.color) }
        }
        .catch { error ->
            reportError(error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images = _images.asStateFlow()
    private val _videos = MutableStateFlow<List<String>>(emptyList())
    val videos = _videos.asStateFlow()
    private val _audios = MutableStateFlow<List<String>>(emptyList())
    val audios = _audios.asStateFlow()
    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()
    private val _lastSaveTime = MutableStateFlow(0L)
    val lastSaveTime = _lastSaveTime.asStateFlow()
    private val _showProUpgradeDialog = MutableStateFlow(false)
    val showProUpgradeDialog = _showProUpgradeDialog.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _isEditMode = MutableStateFlow(noteId == null || noteId == "new")
    val isEditMode = _isEditMode.asStateFlow()
    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen = _isFullScreen.asStateFlow()
    private val _isAutoSaving = MutableStateFlow(false)
    val isAutoSaving = _isAutoSaving.asStateFlow()
    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()
    val revisions = _note.flatMapLatest { note ->
        if (note == null) {
            flowOf(emptyList<NoteRevisionEntity>())
        } else {
            repository.getNoteRevisions(note.id, note.userId)
        }
    }.catch { error ->
        reportError(error)
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var autoSaveJob: Job? = null
    private val gson = Gson()
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private var undoStackCharacters = 0
    private var redoStackCharacters = 0
    private val maxHistorySize = 60
    private val maxHistoryCharacters = 360_000
    private val maxSingleHistorySnapshotCharacters = 120_000
    private var lastHistorySnapshotAtElapsed = 0L
    private val saveMutex = Mutex()
    private var lastPersistedDraft: NoteDraftSignature? = null

    init { loadNote() }

    private fun loadNote() {
        viewModelScope.launch {
            _isLoading.value = noteId != null && noteId != "new"
            try {
                if (noteId == null || noteId == "new") return@launch
                val userId = userRepository.currentUserId.first()
                val note = repository.getNoteById(noteId, userId).getOrNull() ?: return@launch
                applyNoteSnapshot(note)
            } catch (error: Exception) {
                reportError(error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restoreRevision(revision: NoteRevisionEntity) {
        viewModelScope.launch {
            runCatching {
                autoSaveJob?.cancel()
                autoSaveJob = null
                val note = _note.value ?: return@launch
                repository.restoreRevision(revision.id, note.id, note.userId).getOrThrow()
                val restored = repository.getNoteById(note.id, note.userId).getOrThrow()
                    ?: return@launch
                applyNoteSnapshot(restored)
                clearHistoryStacks()
                _lastSaveTime.value = System.currentTimeMillis()
            }.onFailure { error ->
                reportError(error)
            }
        }
    }

    private fun applyNoteSnapshot(note: NoteEntity) {
        _note.value = note
        _title.value = note.title
        _content.value = note.content
        _isPinned.value = note.isPinned
        _isArchived.value = note.isArchived
        _isLocked.value = note.isLocked
        _format.value = note.format
        _type.value = note.type
        _categoryId.value = note.categoryId
        _tags.value = decodeList(note.tags)
        _attachments.value = decodeList(note.attachments)
        lastPersistedDraft = createDraftSignature()
    }

    fun onTitleChange(newTitle: String) { _title.value = newTitle; scheduleAutoSave() }
    fun onContentChange(newContent: String) { updateContent(newContent) }
    fun onEditorActionContentChange(newContent: String) { updateContent(newContent, forceHistory = true) }
    fun onLocationChange(newLocation: String) { _location.value = newLocation; scheduleAutoSave() }
    fun onCategoryChange(newCategoryId: String?) { _categoryId.value = newCategoryId; scheduleAutoSave() }
    fun onFormatChange(newFormat: String) { _format.value = newFormat; scheduleAutoSave() }
    fun onTypeChange(newType: String) { _type.value = newType; scheduleAutoSave() }
    fun addTag(tag: String) {
        val normalized = normalizeTagInput(tag)
        if (normalized.isBlank()) return
        _tags.update { current ->
            if (current.any { it.equals(normalized, ignoreCase = true) }) {
                current
            } else {
                (current + normalized).take(MAX_NOTE_TAGS)
            }
        }
        scheduleAutoSave()
    }
    fun removeTag(tag: String) { _tags.update { it - tag }; scheduleAutoSave() }
    fun addAttachment(uri: String) {
        if (uri.isBlank()) return
        _attachments.update { current ->
            if (current.contains(uri)) current else (current + uri).take(MAX_ATTACHMENTS)
        }
        scheduleAutoSave()
    }
    fun removeAttachment(uri: String) { _attachments.update { it - uri }; scheduleAutoSave() }
    fun addImage(uri: String) { _images.update { it + uri }; addAttachment(uri) }
    fun removeImage(uri: String) { _images.update { it - uri }; removeAttachment(uri) }
    fun addVideo(uri: String) { _videos.update { it + uri }; addAttachment(uri) }
    fun removeVideo(uri: String) { _videos.update { it - uri }; removeAttachment(uri) }
    fun addAudio(uri: String) { _audios.update { it + uri }; addAttachment(uri) }
    fun removeAudio(uri: String) { _audios.update { it - uri }; removeAttachment(uri) }
    fun togglePin() { _isPinned.value = !_isPinned.value; scheduleAutoSave() }
    fun toggleArchive() { _isArchived.value = !_isArchived.value; scheduleAutoSave() }
    fun toggleLock() {
        viewModelScope.launch {
            runCatching {
                if (!_isLocked.value && userPreferences.privacyPassword.first().isNullOrBlank()) {
                    _error.value = context.getString(R.string.note_lock_privacy_required)
                    return@launch
                }
                _isLocked.value = !_isLocked.value
                scheduleAutoSave()
            }.onFailure { error ->
                reportError(error)
            }
        }
    }
    fun dismissProDialog() { _showProUpgradeDialog.value = false }
    fun clearError() { _error.value = null }
    fun toggleEditMode() { _isEditMode.value = !_isEditMode.value }
    fun toggleFullScreen() { _isFullScreen.value = !_isFullScreen.value }
    fun undo() {
        val previous = undoStack.pollLast() ?: return
        undoStackCharacters = (undoStackCharacters - previous.length).coerceAtLeast(0)
        redoStack.addLast(_content.value)
        redoStackCharacters += _content.value.length
        trimHistoryStacks()
        _content.value = previous
        lastHistorySnapshotAtElapsed = SystemClock.elapsedRealtime()
        updateHistoryState()
        scheduleAutoSave()
    }

    fun redo() {
        val next = redoStack.pollLast() ?: return
        redoStackCharacters = (redoStackCharacters - next.length).coerceAtLeast(0)
        rememberContentSnapshot(_content.value)
        _content.value = next
        lastHistorySnapshotAtElapsed = SystemClock.elapsedRealtime()
        updateHistoryState()
        scheduleAutoSave()
    }

    fun saveNote(onSuccess: () -> Unit) {
        autoSaveJob?.cancel()
        autoSaveJob = null
        viewModelScope.launch {
            if (persistNote()) onSuccess()
        }
    }

    fun deleteNote(onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                _note.value?.let { repository.deleteNote(it) }
            }.onSuccess {
                onSuccess()
            }.onFailure { error ->
                reportError(error)
            }
        }
    }

    private fun updateContent(newContent: String, forceHistory: Boolean = false) {
        val current = _content.value
        if (newContent == current) return
        if (forceHistory || shouldRecordHistorySnapshot(current, newContent)) {
            rememberContentSnapshot(current)
        }
        redoStack.clear()
        redoStackCharacters = 0
        _content.value = newContent
        updateHistoryState()
        scheduleAutoSave()
    }

    private fun shouldRecordHistorySnapshot(current: String, next: String): Boolean {
        if (undoStack.isEmpty()) return true
        val now = SystemClock.elapsedRealtime()
        val lengthDelta = kotlin.math.abs(next.length - current.length)
        val textLength = maxOf(current.length, next.length)
        val isDeletion = next.length < current.length
        val isLargeEdit = lengthDelta >= importantEditDeltaFor(textLength)
        val reachedPauseBoundary = now - lastHistorySnapshotAtElapsed >= historySnapshotIntervalFor(textLength)
        val finishedWord = textLength <= WORD_BOUNDARY_HISTORY_MAX_LENGTH &&
            next.length > current.length &&
            next.lastOrNull()?.isWhitespace() == true &&
            current.lastOrNull()?.isWhitespace() != true
        return isDeletion || isLargeEdit || reachedPauseBoundary || finishedWord
    }

    private fun historySnapshotIntervalFor(textLength: Int): Long {
        return when {
            textLength >= 80_000 -> 3_000L
            textLength >= 40_000 -> 2_200L
            textLength >= 16_000 -> 1_500L
            else -> 900L
        }
    }

    private fun importantEditDeltaFor(textLength: Int): Int {
        return when {
            textLength >= 80_000 -> 64
            textLength >= 40_000 -> 40
            textLength >= 16_000 -> 24
            else -> 12
        }
    }

    private fun rememberContentSnapshot(content: String) {
        if (undoStack.peekLast() == content) return
        if (content.length > maxSingleHistorySnapshotCharacters) {
            clearHistoryStacks()
            return
        }
        undoStack.addLast(content)
        undoStackCharacters += content.length
        lastHistorySnapshotAtElapsed = SystemClock.elapsedRealtime()
        trimHistoryStacks()
        updateHistoryState()
    }

    private fun trimHistoryStacks() {
        undoStackCharacters = trimStack(undoStack, undoStackCharacters)
        redoStackCharacters = trimStack(redoStack, redoStackCharacters)
    }

    private fun trimStack(stack: ArrayDeque<String>, currentCharacters: Int): Int {
        var characters = currentCharacters
        while (stack.size > maxHistorySize || characters > maxHistoryCharacters) {
            if (stack.isEmpty()) break
            characters = (characters - stack.removeFirst().length).coerceAtLeast(0)
        }
        return characters
    }

    private fun clearHistoryStacks() {
        undoStack.clear()
        redoStack.clear()
        undoStackCharacters = 0
        redoStackCharacters = 0
        updateHistoryState()
    }

    private fun updateHistoryState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1200)
            persistNote()
        }
    }

    private suspend fun persistNote(): Boolean = saveMutex.withLock {
        if (_title.value.isBlank() && _content.value.isBlank() && _attachments.value.isEmpty()) return@withLock true
        val draft = createDraftSignature()
        if (_note.value != null && draft == lastPersistedDraft) return@withLock true
        _isAutoSaving.value = true
        try {
            val userId = userRepository.currentUserId.first()
            val current = _note.value
            val now = System.currentTimeMillis()
            val isNewPlainNote = current == null && (noteId == null || noteId == "new")
            if (isNewPlainNote && !canCreateAdditionalNote(userId)) {
                _error.value = context.getString(R.string.note_member_limit_message)
                return false
            }
            val note = (current ?: NoteEntity(
                id = if (noteId != null && noteId != "new") noteId else UUID.randomUUID().toString(),
                userId = userId,
                title = _title.value,
                content = _content.value,
                color = "#FFFFFF"
            )).copy(
                title = _title.value,
                content = _content.value,
                createdAt = current?.createdAt ?: now,
                skinId = DEFAULT_NOTE_STYLE_ID,
                isPinned = _isPinned.value,
                isArchived = _isArchived.value,
                isLocked = _isLocked.value,
                format = _format.value,
                tags = encodeList(_tags.value),
                type = _type.value,
                attachments = encodeList(_attachments.value),
                categoryId = _categoryId.value
            )
            if (current == null && repository.getNoteById(note.id, userId).getOrNull() == null) repository.insertNote(note) else repository.updateNote(note)
            _note.value = note
            lastPersistedDraft = draft
            _lastSaveTime.value = now
            true
        } catch (e: Exception) {
            reportError(e)
            false
        } finally {
            _isAutoSaving.value = false
        }
    }

    private fun createDraftSignature(): NoteDraftSignature {
        val currentContent = _content.value
        return NoteDraftSignature(
            title = _title.value,
            contentLength = currentContent.length,
            contentHash = currentContent.hashCode(),
            isPinned = _isPinned.value,
            isArchived = _isArchived.value,
            isLocked = _isLocked.value,
            format = _format.value,
            tags = _tags.value,
            type = _type.value,
            attachments = _attachments.value,
            categoryId = _categoryId.value
        )
    }

    private suspend fun canCreateAdditionalNote(userId: String): Boolean {
        if (userRepository.isUserPro(userId)) return true
        val currentNoteCount = repository.getAllNotes(userId).first().count { it.isVisibleStandaloneNote() }
        return currentNoteCount < FREE_NOTE_LIMIT
    }

    private fun encodeList(values: List<String>): String? = if (values.isEmpty()) null else gson.toJson(values)

    private fun decodeList(value: String?): List<String> = try {
        if (value.isNullOrBlank()) emptyList() else gson.fromJson(value, Array<String>::class.java).toList()
    } catch (_: Exception) {
        emptyList()
    }

    private fun normalizeTagInput(value: String): String {
        return value
            .trim()
            .trimStart('#')
            .replace(Regex("\\s+"), " ")
            .take(MAX_TAG_LENGTH)
    }

    private companion object {
        const val FREE_NOTE_LIMIT = 3
        const val DEFAULT_NOTE_STYLE_ID = "classic"
        const val WORD_BOUNDARY_HISTORY_MAX_LENGTH = 16_000
        const val MAX_NOTE_TAGS = 12
        const val MAX_TAG_LENGTH = 24
        const val MAX_ATTACHMENTS = 20
    }

    override fun onCleared() {
        autoSaveJob?.cancel()
        super.onCleared()
    }

    private fun reportError(error: Throwable) {
        ErrorReporter.reportException(context, error)
        _error.value = error.message ?: context.getString(R.string.operation_failed)
    }
}

private data class NoteDraftSignature(
    val title: String,
    val contentLength: Int,
    val contentHash: Int,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isLocked: Boolean,
    val format: String,
    val tags: List<String>,
    val type: String,
    val attachments: List<String>,
    val categoryId: String?
)

data class NoteFolderOption(
    val id: String,
    val name: String,
    val color: String
)
