package com.example.gmailapitest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _messages = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val messages: StateFlow<List<Map<String, String>>> get() = _messages

    private val _userAuthenticated: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val userAuthenticated: StateFlow<Boolean> get() = _userAuthenticated
    private val _loadingMessages: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingMessages: StateFlow<Boolean> get() = _loadingMessages

    val prompt = "You are expert in generating json objects. At the end of this message, you will be given a subject text of an email which is sent by indian banks for sending user's credit card statement. Extract following information from the text and send a json response in the format described below:" +
            "Example input:" +
            "ICICI Bank Credit Card Statement for account xxx9000 for INR 30,411.69 from 19th Jan to 18th Feb" +
            "Example response: {bank: \"ICICI Bank\", ending_digits: \"9000\", due_date: \"7th Mar\", amount: 30411.69 }" +
            "Keep in mind, in the subject text, the date information is the time period of the credit card statement, from its end date, the due date is around 20 days after. " +
            "Only return the json object as response with all keys defined. If no data is found corresponding to any key in json object, then assign null value to them. No other string is to be entered apart from it.\n"

    fun updateMessages(messagesDetails: List<Map<String, String>>) {
        val mutableDetails = messagesDetails.toMutableList()
        val mutableMaps = mutableDetails.map { it.toMutableMap() }

        viewModelScope.launch(Dispatchers.IO) {
            _loadingMessages.value = true
            mutableMaps.map { message ->
                message["subject"] = getCreditCardInfo(message["subject"]) ?: ""
                message["body"] = getCreditCardInfo(message["body"]) ?: message["body"] ?: ""
            }
            _messages.value = mutableMaps
            _loadingMessages.value = mutableMaps.isEmpty()
        }
    }

    fun setAuthenticated(isAuthenticated: Boolean) {
        _userAuthenticated.value = isAuthenticated
    }

    val geminiModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun getCreditCardInfo(info: String?): String? {
        return geminiModel.generateContent(prompt + info).text?.replace("```json", "")?.replace("```", "")
    }
}