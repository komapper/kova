package org.komapper.extension.validator

import kotlin.reflect.KProperty0

context(_: Validation)
inline fun <T : Any> T.schema(block: context(Validation) () -> Unit) {
    val klass = this::class
    val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
    return addRoot(rootName, this, block)
}

@IgnorableReturnValue
context(_: Validation, _: Accumulate)
operator fun <T> KProperty0<T>.invoke(block: Constraint<T>): Accumulate.Value<Unit> {
    val value = this.get()
    return addPathChecked(name, value) { accumulating { block(value) } } ?: Accumulate.Ok(Unit)
}
