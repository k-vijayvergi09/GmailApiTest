package com.example.gmailapitest.model

import com.google.gson.annotations.SerializedName

data class CreditCardInfo(
    val bank: String,
    @SerializedName("ending_digits") val endingDigits: String,
    @SerializedName("due_date") val dueDate: String,
    val amount: Double
)