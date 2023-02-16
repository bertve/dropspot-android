package com.example.dropspot.data.model

import java.io.Serializable

data class Address(
    val street: String = "",
    val houseNumber: String = "",
    val postalCode: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = ""
) : Serializable {

    fun getAddressString(): String {
        return "${this.street} ${this.houseNumber}\n" +
            "${this.postalCode} ${this.city}\n"
    }
}
