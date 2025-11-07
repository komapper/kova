package org.komapper.extension.validator

data class ValidationContext(
    val root: String = "",
    val path: String = "",
    val failFast: Boolean = false,
)

fun ValidationContext.addRoot(root: String): ValidationContext {
    val self = this
    return copy(
        root =
            buildString {
                if (self.root.isNotEmpty()) {
                    append(self.root)
                } else {
                    append(root)
                }
            },
    )
}

fun ValidationContext.addPath(path: String): ValidationContext {
    val self = this
    return copy(
        path =
            buildString {
                append(self.path)
                if (self.path.isNotEmpty() && path.isNotEmpty()) append(".")
                append(path)
            },
    )
}

fun ValidationContext.appendPath(path: String): ValidationContext = copy(path = this.path + path)

fun <T> ValidationContext.createConstraintContext(input: T): ConstraintContext<T> = ConstraintContext(input, root, path, failFast)

fun ConstraintContext<*>.createValidationContext(): ValidationContext = ValidationContext(root, path, failFast)
