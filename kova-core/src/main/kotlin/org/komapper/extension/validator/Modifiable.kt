package org.komapper.extension.validator

interface Modifiable<T, R> {
    fun modify(
        name: String,
        transform: (T) -> T,
    ): R
}
