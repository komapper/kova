package org.komapper.extension.validator

import kotlin.reflect.KProperty0

context(_: Validation)
inline fun <T : Any> T.checking(block: context(Validation) () -> ValidationResult<Unit>): ValidationResult<Unit> {
    val klass = this::class
    val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
    return addRoot(rootName, this, block)
}

context(_: Validation)
operator fun <T> KProperty0<T>.invoke(block: Constraint<T>): ValidationResult<Unit> {
    val value = this.get()
    return addPathChecked(name, value) { block(value).accumulateMessages() } ?: Unit.success()
}
