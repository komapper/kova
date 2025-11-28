package org.komapper.extension.validator

interface Modifiable<T, R> {
    fun modify(transform: (T) -> T): R
}
