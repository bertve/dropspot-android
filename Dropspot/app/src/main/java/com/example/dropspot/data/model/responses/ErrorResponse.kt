package com.example.dropspot.data.model.responses

import java.time.LocalDateTime

data class ErrorResponse(
    private val timestamp: LocalDateTime,
    private val status: Int,
    private val error: String
)
