package dev.alimansour.mytasks.core.domain.model

sealed interface DataError : Error {
    enum class Remote : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        INVALID_CREDENTIALS,
        EMAIL_NOT_VERIFIED,
        ACCESS_TOKEN_EXPIRED,
        USER_NOT_AUTHORIZED,
        FirebaseAuthUserCollision,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN,
        DATABASE_READ_ERROR,
        DATABASE_WRITE_ERROR,
    }
}
