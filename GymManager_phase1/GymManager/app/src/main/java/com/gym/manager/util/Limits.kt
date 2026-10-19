package com.gym.manager.util

/** Validation limits — taken from the original Python file. */
object Limits {
    const val MIN_DAYS = 1
    const val MAX_DAYS = 3650
    const val MIN_PRICE = 0L
    const val MAX_PRICE = 1_000_000L
    const val MAX_PAYMENT_AMOUNT = 1_000_000L
    const val MAX_PAYMENT_DAYS = 10_000
    const val PHONE_MIN = 10
    const val PHONE_MAX = 15
}

object PaymentTypes {
    const val SUBSCRIPTION = "subscription"
    const val RENEWAL = "renewal"
    const val EXTRA_DAYS = "extra_days"
    const val ADDITIONAL = "additional_payment"
    const val PRICE_UPDATE = "price_update"

    /** Only these types count as income (price_update is a record, not money). */
    val income = listOf(SUBSCRIPTION, RENEWAL, EXTRA_DAYS, ADDITIONAL)
    val all = income + PRICE_UPDATE
}

object PaymentMethods {
    val all = listOf("cash", "card", "transfer", "wallet", "other")
}
