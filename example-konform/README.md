# Konform Comparison

This example demonstrates Kova validation capabilities through side-by-side comparisons with [Konform](https://www.konform.io/), a type-safe Kotlin DSL validation library.

## Overview

Each test class contains parallel implementations of the same validation scenarios using both Konform and Kova, allowing direct comparison of approaches, syntax, and features.

See the [main README](../README.md) for core Kova validation concepts.

## Comparison Tests

### Simple Validation ([SimpleTest.kt](src/test/kotlin/example/konform/SimpleTest.kt))

Basic field-level validation on a `UserProfile` class demonstrating:

**Konform:**
```kotlin
val validateUser = Validation {
    UserProfile::fullName {
        minLength(2)
        maxLength(100)
    }
    UserProfile::age ifPresent {
        minimum(0)
        maximum(150)
    }
}

val result = validateUser(userProfile)
```

**Kova:**
```kotlin
fun Validation.validate(userProfile: UserProfile) = userProfile.schema {
    userProfile::fullName {
        minLength(it, 2)
        maxLength(it, 100)
    }
    userProfile::age {
        if (it != null) inRange(it, 0..150)
    }
}

val result = tryValidate { validate(userProfile) }
```

**Key Differences:**
- Konform uses a builder DSL that returns a validation function; Kova uses extension functions in a validation context
- Konform has `ifPresent` for nullable handling; Kova uses explicit `if (it != null)` checks
- Konform validators don't take the value as parameter; Kova uses input-first parameters
- Kova returns a typed `ValidationResult<T>`; Konform returns a `ValidationResult` with error list

### Custom Validators ([CustomValidatorTest.kt](src/test/kotlin/example/konform/CustomValidatorTest.kt))

Creating custom validation logic with custom messages:

**Konform:**
```kotlin
val validateUser = Validation {
    UserProfile::fullName {
        constrain("Name cannot contain a tab") { !it.contains("\t") }
        constrain(
            "Name must have a non-whitespace character",
            path = ValidationPath.of("trimmedName")
        ) {
            it.trim().isNotEmpty()
        }
        constrain("Must have 5 characters", userContext = Severity.ERROR) {
            it.length >= 5
        }
    }
}
```

**Kova:**
```kotlin
fun Validation.validate(userProfile: UserProfile) = userProfile.schema {
    userProfile::fullName {
        notContains(it, "\t") { text("Name cannot contain a tab") }
        notBlank(it) { text("Name must have a non-whitespace character") }
        minLength(it, 5) { text("Must have 5 characters") }
    }
}
```

**Key Differences:**
- Konform uses `constrain(message) { predicate }` for custom validators
- Kova provides built-in validators (`notContains`, `notBlank`) that can override messages
- Konform supports custom path names and user context for errors
- Kova expresses custom messages via `text()` provider in validation blocks

### Collection Validation ([CollectionTest.kt](src/test/kotlin/example/konform/CollectionTest.kt))

Nested object validation with collections (lists and maps):

**Konform:**
```kotlin
val validateEvent = Validation {
    Event::organizer {
        Person::email required {
            hint = "Email address must be given"
            pattern(".+@bigcorp.com") hint "Organizers must have a BigCorp email address"
        }
    }
    Event::attendees {
        maxItems(100)
    }
    Event::attendees onEach {
        Person::name {
            minLength(2)
        }
        Person::age {
            minimum(18) hint "Attendees must be 18 years or older"
        }
        Person::email ifPresent {
            pattern(".+@.+\\..+") hint "Please provide a valid email address (optional)"
        }
    }
    Event::ticketPrices {
        minItems(1) hint "Provide at least one ticket price"
    }
    Event::ticketPrices onEach {
        Map.Entry<String, Double?>::value ifPresent {
            minimum(0.01)
        }
    }
}
```

**Kova:**
```kotlin
fun Validation.validateOrganizer(person: Person) = person.schema {
    person::email {
        notNull(it) { text("Email address must be given") }
        matches(it, Regex(".+@bigcorp.com")) {
            text("Organizers must have a BigCorp email address")
        }
    }
}

fun Validation.validateAttendee(person: Person) = person.schema {
    person::name { minLength(it, 2) }
    person::age { minValue(it, 18) { text("Attendees must be 18 years or older") } }
    person::email {
        if (it != null) matches(it, Regex(".+@.+\\..+")) {
            text("Please provide a valid email address (optional)")
        }
    }
}

fun Validation.validate(event: Event) = event.schema {
    event::organizer { validateOrganizer(it) }
    event::attendees {
        maxSize(it, 100)
        onEach(it) { validateAttendee(it) }
    }
    event::ticketPrices {
        minSize(it, 1) { text("Provide at least one ticket price") }
        onEachValue(it) { price ->
            if (price != null) minValue(price, 0.01)
        }
    }
}
```

**Key Differences:**
- Konform validates nested properties inline using property references
- Kova uses explicit validation functions for nested objects (`validateOrganizer`, `validateAttendee`)
- Konform's `onEach` operates directly on property references; Kova's `onEach` takes the collection as input
- Kova provides `onEachValue` for map values; Konform uses `onEach` with `Map.Entry::value`
- Kova allows easy extraction of reusable validation logic as extension functions
- Kova groups collection element errors under a parent message with `descendants` property

### Reusable Validation Logic ([SplitTest.kt](src/test/kotlin/example/konform/SplitTest.kt))

Extracting and reusing validation logic across multiple properties:

**Konform:**
```kotlin
val ageCheck = Validation<Int?> {
    required {
        minimum(21)
    }
}

val validateUser = Validation {
    UserProfile::age {
        run(ageCheck)
    }

    validate("ageMinus10", { it.age?.let { age -> age - 10 } }) {
        run(ageCheck)
    }
}
```

**Kova:**
```kotlin
fun Validation.checkAge(age: Int?) {
    notNull(age)
    minValue(age, 21)
}

fun Validation.validate(userProfile: UserProfile) = userProfile.schema {
    userProfile::age {
        checkAge(it)
    }
    name("ageMinus10") {
        checkAge(userProfile.age?.let { age -> age - 10 })
    }
}
```

**Key Differences:**
- Konform uses `run()` to execute a separate validation object
- Kova simply calls extension functions like any other Kotlin function
- Konform requires `validate(name, selector)` to validate computed values; Kova uses `name()` block
- Kova's `notNull()` uses Kotlin contracts for smart casting, allowing subsequent validators to work with non-nullable types
- Kova's approach is more natural to Kotlin - no special `run()` function needed

### Recursive Structures ([RecursiveTest.kt](src/test/kotlin/example/konform/RecursiveTest.kt))

Handling circular references in recursive data structures:

**Konform:**
```kotlin
val validationNode = Validation {
    Node::children {
        maxItems(2)
    }
    Node::children onEach {
        runDynamic { validationRef }
    }
}
private val validationRef get(): Validation<Node> = validationNode

// Note: Results in stack overflow with circular references
```

**Kova:**
```kotlin
fun Validation.validate(node: Node) {
    node.schema {
        node::children {
            maxSize(it, 2)
        }
    }
}

// Handles circular references gracefully - no stack overflow
```

**Key Differences:**
- Konform can cause stack overflow with circular references
- Kova detects circular references automatically via `Validation.addPathChecked()`
- Kova skips already-visited objects in the validation path, preventing infinite recursion
- Konform requires `runDynamic` for recursive validation; Kova uses regular function calls

### Dynamic Validation ([DynamicTest.kt](src/test/kotlin/example/konform/DynamicTest.kt))

Applying different validation rules based on the data being validated:

**Konform:**
```kotlin
val validateAddress = Validation {
    dynamic { address ->
        Address::postalCode {
            when (address.countryCode) {
                "US" -> pattern("[0-9]{5}")
                else -> pattern("[A-Z]+")
            }
        }
    }
}
```

**Kova:**
```kotlin
fun Validation.validate(address: Address) = address.schema {
    address::postalCode {
        when (address.countryCode) {
            "US" -> matches(it, Regex("[0-9]{5}"))
            else -> matches(it, Regex("[A-Z]+"))
        }
    }
}
```

**Key Differences:**
- Konform requires `dynamic { }` block to access the object being validated
- Kova allows direct access to the object in scope - no special syntax needed
- Kova can use any Kotlin control flow naturally (when, if, for, etc.)
- Konform's dynamic block adds indirection; Kova is straightforward

## Running the Tests

```bash
./gradlew example-konform:test
```

## Key Observations

| Aspect                  | Konform                                     | Kova                                        |
|-------------------------|---------------------------------------------|---------------------------------------------|
| **API Style**           | Builder DSL with property references        | Context-based extension functions           |
| **Parameter Style**     | Implicit (property value in scope)          | Explicit input-first parameters             |
| **Nullable Handling**   | `ifPresent`, `required`                     | Explicit null checks or `notNull`           |
| **Custom Messages**     | `hint` keyword                              | Message provider lambdas                    |
| **Composition**         | `run()` with validation objects             | Regular Kotlin function calls               |
| **Collection Errors**   | Flat list with indexed paths                | Hierarchical with parent/descendants        |
| **Return Type**         | `ValidationResult` with error list          | `ValidationResult<T>` (Success/Failure ADT) |
| **Circular References** | Stack overflow with recursive structures    | Automatic detection and prevention          |
| **Dynamic Validation**  | Requires `dynamic { }` block                | Direct access via natural Kotlin syntax     |

## See Also

- [Main README](../README.md) - Core Kova validation concepts
- [Konform Documentation](https://www.konform.io/) - Official Konform documentation
