package org.komapper.extension.validator

data class ValidationContext(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val logs: List<String> = emptyList(),
    val config: ValidationConfig = ValidationConfig(),
) {
    val failFast: Boolean get() = config.failFast
    val logging: Boolean get() = config.logging
}

data class ValidationConfig(
    val failFast: Boolean = false,
    val logging: Boolean = false,
)

fun ValidationContext.addRoot(
    name: String,
    obj: Any?,
): ValidationContext =
    if (root.isEmpty()) {
        copy(root = name, path = path.copy(obj = obj))
    } else {
        this
    }

fun ValidationContext.addPath(
    name: String,
    obj: Any?,
): ValidationContext {
    val parent = this.path
    val path =
        parent.copy(
            name = name,
            obj = obj,
            parent = parent,
        )
    return copy(path = path)
}

fun <T> ValidationContext.addPathChecked(
    name: String,
    obj: T,
): ValidationResult<T> {
    val parent = this.path
    // Check for circular reference
    if (obj != null && parent.containsObject(obj)) {
        // Return failure to signal circular reference detection
        // The caller will convert this to success and terminate validation
        return ValidationResult.Failure(
            FailureDetail.Single(
                this,
                Message.Text("Circular reference detected."),
                null,
            ),
        )
    }
    return ValidationResult.Success(obj, addPath(name, obj))
}

fun ValidationContext.addLog(log: String): ValidationContext = if (logging) copy(logs = this.logs + log) else this

fun ValidationContext.appendPath(text: String): ValidationContext {
    val path = this.path.copy(name = this.path.name + text)
    return copy(path = path)
}

fun <T> ValidationContext.createConstraintContext(input: T): ConstraintContext<T> =
    ConstraintContext(input = input, validationContext = this)

data class Path(
    val name: String,
    val obj: Any?,
    val parent: Path?,
) {
    val fullName: String
        get() {
            if (parent == null || parent.name.isEmpty()) return name
            return if (name.isEmpty()) parent.fullName else "${parent.fullName}.$name"
        }

    fun containsObject(target: Any): Boolean {
        if (obj === target) return true
        return parent?.containsObject(target) ?: false
    }
}
