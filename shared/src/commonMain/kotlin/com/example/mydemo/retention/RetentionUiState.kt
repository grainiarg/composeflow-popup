package com.example.mydemo.retention

sealed interface RetentionUiState {
    data object Loading : RetentionUiState
    data object Empty : RetentionUiState
    data class Success(val rootNode: DslNode) : RetentionUiState
    data class Error(val errors: List<String>) : RetentionUiState
}
