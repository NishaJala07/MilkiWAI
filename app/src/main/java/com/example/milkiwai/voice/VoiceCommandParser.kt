package com.example.milkiwai.voice

import java.util.*

class VoiceCommandParser {

    fun parse(text: String): ParsedCommand {
        val lowerText = text.lowercase(Locale.getDefault())

        return when {
            lowerText.contains("add customer") -> {
                val data = mutableMapOf<String, String>()
                // Simplified extraction logic: "add customer [Name] building [Building] room [Room]"
                val nameMatch = Regex("customer\\s+([a-zA-Z]+)").find(lowerText)
                val buildingMatch = Regex("building\\s+([a-zA-Z0-9\\s]+)").find(lowerText)
                val roomMatch = Regex("room\\s+([a-zA-Z0-9]+)").find(lowerText)
                val mobileMatch = Regex("mobile\\s+([0-9]+)").find(lowerText)
                
                nameMatch?.groupValues?.get(1)?.let { data["name"] = it }
                buildingMatch?.groupValues?.get(1)?.let { data["building"] = it.trim() }
                roomMatch?.groupValues?.get(1)?.let { data["room"] = it }
                mobileMatch?.groupValues?.get(1)?.let { data["mobile"] = it }
                
                ParsedCommand(VoiceAction.ADD_CUSTOMER, data)
            }
            
            lowerText.contains("generate bills") || lowerText.contains("generate bill") -> {
                ParsedCommand(VoiceAction.GENERATE_BILLS)
            }
            
            lowerText.contains("pending payments") || lowerText.contains("show pending") -> {
                ParsedCommand(VoiceAction.SHOW_PENDING)
            }
            
            lowerText.contains("buy tomorrow") || lowerText.contains("procurement") || lowerText.contains("milk requirement") -> {
                ParsedCommand(VoiceAction.PROCUREMENT_REPORT)
            }

            lowerText.contains("show") && lowerText.contains("route") -> {
                ParsedCommand(VoiceAction.SHOW_ROUTE)
            }

            lowerText.contains("mark") && lowerText.contains("delivered") -> {
                val data = mutableMapOf<String, String>()
                val matches = Regex("mark\\s+([a-zA-Z]+)\\s+delivered").find(lowerText)
                matches?.let { data["customer"] = it.groupValues[1] }
                ParsedCommand(VoiceAction.MARK_DELIVERED, data)
            }

            lowerText.contains("mark") && lowerText.contains("missed") -> {
                val data = mutableMapOf<String, String>()
                val matches = Regex("mark\\s+([a-zA-Z]+)\\s+missed").find(lowerText)
                matches?.let { data["customer"] = it.groupValues[1] }
                ParsedCommand(VoiceAction.MARK_MISSED, data)
            }

            lowerText.contains("pause") -> {
                val data = mutableMapOf<String, String>()
                // Heuristic: "pause [Name] from [Date] to [Date]"
                val matches = Regex("pause\\s+([a-zA-Z]+)").find(lowerText)
                matches?.let { data["customer"] = it.groupValues[1] }
                ParsedCommand(VoiceAction.PAUSE_DELIVERY, data)
            }

            // "Nisha Amul Gold 3 litre from 15 June to 20 June" -> Quantity Change
            lowerText.contains("from") && lowerText.contains("to") && (lowerText.contains("litre") || lowerText.contains("liter")) -> {
                val data = mutableMapOf<String, String>()
                val matches = Regex("([a-zA-Z]+)\\s+([a-zA-Z\\s]+)\\s+(\\d+)\\s+litre\\s+from\\s+([0-9a-zA-Z\\s]+)\\s+to\\s+([0-9a-zA-Z\\s]+)").find(lowerText)
                matches?.let {
                    data["customer"] = it.groupValues[1]
                    data["brand"] = it.groupValues[2].trim()
                    data["quantity"] = it.groupValues[3]
                    data["start"] = it.groupValues[4].trim()
                    data["end"] = it.groupValues[5].trim()
                }
                ParsedCommand(VoiceAction.QTY_CHANGE, data)
            }

            // "Nisha Amul Gold 2 litre" -> ADD_SUBSCRIPTION
            // This is harder to parse without context of brand names, but let's try a heuristic
            lowerText.contains("litre") || lowerText.contains("liter") -> {
                val data = mutableMapOf<String, String>()
                // Heuristic: [Name] [Brand] [Qty] litre
                val matches = Regex("([a-zA-Z]+)\\s+([a-zA-Z\\s]+)\\s+(\\d+(\\.\\d+)?)\\s+litre").find(lowerText)
                matches?.let {
                    data["customer"] = it.groupValues[1]
                    data["brand"] = it.groupValues[2].trim()
                    data["quantity"] = it.groupValues[3]
                }
                ParsedCommand(VoiceAction.ADD_SUBSCRIPTION, data)
            }

            else -> ParsedCommand(VoiceAction.UNKNOWN)
        }
    }
}
