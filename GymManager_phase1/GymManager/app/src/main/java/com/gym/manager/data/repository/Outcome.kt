package com.gym.manager.data.repository

/** Result of a repository operation, shown to the admin as-is. */
sealed interface Outcome {
    data object Ok : Outcome
    data class Fail(val message: String) : Outcome
    /** The operation needs the admin to confirm before it is repeated with confirmation. */
    data class Confirm(val message: String) : Outcome
}
