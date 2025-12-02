package org.komapper.extension.validator

data class ValidationContext(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val failFast: Boolean = false,
    val logs: List<String> = emptyList(),
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
    val fullName: String get() {
        if (parent == null || parent.name.isEmpty()) return name
        return if (name.isEmpty()) parent.fullName else "${parent.fullName}.$name"
    }
}
