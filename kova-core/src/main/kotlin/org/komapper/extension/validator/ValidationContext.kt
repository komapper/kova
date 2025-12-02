package org.komapper.extension.validator

data class ValidationContext(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val failFast: Boolean = false,
    val logs: List<String> = emptyList(),
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
        return ValidationResult.Failure.Simple(
            listOf(
                ValidationResult.FailureDetail(
                    this,
                    Message.Text("Circular reference detected."),
                    null,
                ),
            ),
        )
    }
    return ValidationResult.Success(obj, addPath(name, obj))
}

fun ValidationContext.addLog(log: String): ValidationContext = copy(logs = this.logs + log)

fun ValidationContext.appendPath(text: String): ValidationContext {
    val path = this.path.copy(name = this.path.name + text)
    return copy(path = path)
}

fun <T> ValidationContext.createConstraintContext(input: T): ConstraintContext<T> = ConstraintContext(input, root, path, failFast)

fun ConstraintContext<*>.createValidationContext(): ValidationContext = ValidationContext(root, path, failFast)

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
