# Hibernate Validator Comparison

This example demonstrates Kova validation capabilities through side-by-side comparisons with [Hibernate Validator](https://hibernate.org/validator/), the reference implementation of Jakarta Bean Validation.

## Overview

Each test class contains parallel implementations of the same validation scenarios using both Hibernate Validator and Kova, allowing direct comparison of approaches, syntax, and features. Examples are based on the [Hibernate Validator Reference Guide](https://github.com/hibernate/hibernate-validator/tree/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide).

See the [main README](../README.md) for core Kova validation concepts.

## Comparison Tests

### Simple Validation ([SimpleTest.kt](src/test/kotlin/example/hibernate/validator/SimpleTest.kt))

Basic field-level constraints on a `Car` class demonstrating:

**Hibernate Validator:**
```kotlin
class Car(
    @field:NotNull
    val manufacturer: String?,
    @field:Size(min = 2, max = 14)
    @field:NotNull
    val licensePlate: String?,
    @field:Min(2)
    val seatCount: Int
)

val validator = Validation.buildDefaultValidatorFactory().validator
val violations = validator.validate(car)
```

**Kova:**
```kotlin
class Car(
    val manufacturer: String?,
    val licensePlate: String?,
    val seatCount: Int
)

fun Validation.validate(car: Car) = car.schema {
    car::manufacturer { notNull(it) }
    car::licensePlate {
        val v = toNonNullable(it)
        minLength(v, 2)
        maxLength(v, 14)
    }
    car::seatCount { minValue(it, 2) }
}

val result = tryValidate { validate(car) }
```

**Key Differences:**
- Hibernate Validator uses annotations; Kova uses extension functions
- Hibernate Validator validates on nullable types; Kova requires explicit nullable handling
- Kova returns a typed `ValidationResult<T>`; Hibernate Validator returns `Set<ConstraintViolation>`

### Object Graph Validation ([ObjectGraphTest.kt](src/test/kotlin/example/hibernate/validator/ObjectGraphTest.kt))

Nested object validation with `@Valid` cascading (Hibernate) vs. explicit validation calls (Kova):

**Hibernate Validator:**
```kotlin
class Car(
    @field:NotNull
    @field:Valid
    val driver: Person?
)
```

**Kova:**
```kotlin
fun Validation.validate(car: Car) = car.schema {
    car::driver {
        val v = toNonNullable(it)
        validate(v)  // Explicit nested validation
    }
}
```

### Validation Groups ([GroupTest.kt](src/test/kotlin/example/hibernate/validator/GroupTest.kt))

Conditional validation based on context (e.g., DEFAULT, CAR, DRIVER checks):

**Kova Approach:**
```kotlin
fun Validation.validate(
    car: Car,
    checks: Set<Check> = setOf(Check.DEFAULT)
) = car.schema {
    if (Check.DEFAULT in checks) {
        car::manufacturer { notNull(it) }
        // ... other default checks
    }

    if (Check.CAR in checks) {
        car::passedVehicleInspection {
            eq(it, true) { text("The car has to pass the vehicle inspection first") }
        }
    }
}
```

**Note:** This demonstrates Kova's approach to validation groups using explicit conditional logic rather than annotation-based group interfaces.

### Constraint Composition ([ConstraintCompositionTest.kt](src/test/kotlin/example/hibernate/validator/ConstraintCompositionTest.kt))

Reusable validation logic by composing multiple constraints:

**Hibernate Validator:**
```kotlin
// Define a composed constraint using meta-annotations
@NotNull
@Size(min = 2, max = 14)
@CheckCase(CaseMode.UPPER)
@Target(METHOD, FIELD, ANNOTATION_TYPE, TYPE_USE)
@Retention(RUNTIME)
@Constraint(validatedBy = [])
annotation class ValidLicensePlate(
    val message: String = "{ValidLicensePlate.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

// Use the composed constraint
class Car(
    @field:ValidLicensePlate
    val licensePlate: String
)
```

**Kova:**
```kotlin
fun Validation.validateLicensePlate(licensePlate: String) {
    val v = toNonNullable(licensePlate)
    minLength(v, 2)
    maxLength(v, 14)
    uppercase(v)
}

fun Validation.validate(car: Car) = car.schema {
    car::licensePlate { validateLicensePlate(it) }
}
```

**Key Differences:**
- Hibernate Validator requires creating custom annotation types with meta-annotations
- Kova uses regular extension functions that can be called and composed like any function
- Hibernate Validator composition is declarative; Kova composition is programmatic

### Class-Level Constraints ([ClassLevelConstraintTest.kt](src/test/kotlin/example/hibernate/validator/ClassLevelConstraintTest.kt))

Cross-field validation that validates relationships between multiple properties:

**Hibernate Validator:**
```kotlin
// Define the class-level constraint annotation
@Target(TYPE, ANNOTATION_TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidPassengerCountValidator::class])
annotation class ValidPassengerCount(
    val message: String = "There must be not more passengers than seats.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

// Implement the validator
class ValidPassengerCountValidator : ConstraintValidator<ValidPassengerCount, Car> {
    override fun isValid(car: Car?, context: ConstraintValidatorContext): Boolean {
        if (car == null) return true
        return car.passengers.size <= car.seatCount
    }
}

// Apply to the class
@ValidPassengerCount
class Car(
    val seatCount: Int,
    val passengers: List<Person>
)
```

**Kova:**
```kotlin
fun Validation.validate(car: Car) = car.schema {
    car.constrain("validPassengerCount") {
        satisfies(it.passengers.size <= it.seatCount) {
            text("There must be not more passengers than seats.")
        }
    }
}
```

**Key Differences:**
- Hibernate Validator requires three parts: annotation definition, validator class, and annotation application
- Kova expresses the constraint inline using `constrain()` and `satisfies()`
- Hibernate Validator separates constraint declaration from logic; Kova keeps them together

### Custom Path Names ([CustomPathTest.kt](src/test/kotlin/example/hibernate/validator/CustomPathTest.kt))

Controlling error path names for class-level constraints:

**Kova:**
```kotlin
fun Validation.validate(car: Car) = car.schema {
    name("passengers") {  // Custom path name
        car.constrain("validPassengerCount") {
            satisfies(it.passengers.size <= it.seatCount) {
                text("There must be not more passengers than seats.")
            }
        }
    }
}
```

## Running the Tests

```bash
./gradlew example-hibernate-validator:test
```

## Key Observations

| Aspect             | Hibernate Validator                      | Kova                                                 |
|--------------------|------------------------------------------|------------------------------------------------------|
| **Configuration**  | Annotation-based                         | Function-based (DSL)                                 |
| **Type Safety**    | Runtime reflection                       | Compile-time type checking                           |
| **Composition**    | Meta-annotations                         | Regular function calls                               |
| **Null Handling**  | Implicit (annotations on nullable types) | Explicit (`toNonNullable`, `isNull`)                 |
| **Error Handling** | Exception or violation set               | Exception or `ValidationResult<T>` (Success/Failure) |
| **Extensibility**  | Custom validators + annotations          | Extension functions                                  |

## See Also

- [Main README](../README.md) - Core Kova validation concepts
- [Hibernate Validator Reference Guide](https://github.com/hibernate/hibernate-validator/tree/9.1/documentation)
