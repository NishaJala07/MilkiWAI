package com.example.milkiwai.voice

enum class VoiceAction {
    ADD_CUSTOMER,
    ADD_SUBSCRIPTION,
    PAUSE_DELIVERY,
    GENERATE_BILLS,
    SHOW_PENDING,
    PROCUREMENT_REPORT,
    QTY_CHANGE,
    MARK_DELIVERED,
    MARK_MISSED,
    SHOW_ROUTE,
    UNKNOWN
}

data class ParsedCommand(
    val action: VoiceAction,
    val data: Map<String, String> = emptyMap()
)
