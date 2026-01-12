# Kova vs Other Libraries

This document provides detailed code comparisons between Kova and other validation libraries.

For a quick feature comparison, see the [Why Kova?](../README.md#why-kova) section in the README.

## Kotlin-Native Design

Kova is designed specifically for Kotlin. Hibernate Validator, being a Java library, requires annotation use-site targets:

```kotlin
// Hibernate Validator - @field: prefix required in Kotlin
class User(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,
)

// Kova - natural Kotlin syntax
context(_: Validation)
fun User.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthAtMost(100) }
}
```

## Simple Custom Validators

In Hibernate Validator, creating a custom constraint requires an annotation class plus a validator class. In Kova, it's just a function:

```kotlin
// Kova - custom validator is just a function
context(_: Validation)
fun String.ensureNoTabCharacters() = constrain("noTabs") {
    satisfies(!it.contains("\t")) { text("must not contain tabs") }
}
```

### Hibernate Validator Equivalent

```java
// Step 1: Define the annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoTabsValidator.class)
public @interface NoTabs {
    String message() default "must not contain tabs";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Step 2: Implement the validator
public class NoTabsValidator implements ConstraintValidator<NoTabs, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !value.contains("\t");
    }
}
```

## Simple Cross-Property Validation

Comparing multiple properties is straightforward in Kova:

```kotlin
context(_: Validation)
fun Car.validate() = schema {
    ::seatCount { it.ensurePositive() }
    ::passengers { it.ensureNotNull() }

    // Cross-property validation - just access both properties
    constrain("passengerCount") {
        satisfies(it.passengers.size <= it.seatCount) {
            text("Passengers cannot exceed seat count")
        }
    }
}
```

In Hibernate Validator, this requires a class-level constraint annotation with a custom `ConstraintValidator` implementation.

## Argument Validation with `capture`

Kova can validate and transform function arguments using `capture`:

```kotlin
data class User(val name: String, val age: Int)

context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank() }
    val age by capture { rawAge.transformToInt().ensurePositive() }
    return User(name, age)
}

// Collects ALL errors across both fields
val result = tryValidate { buildUser("", "invalid") }
// Errors: name -> "must not be blank", age -> "must be a valid integer"
```

This pattern is not directly supported by Hibernate Validator or Konform, which focus on validating existing objects rather than constructing validated objects from raw input.

## Function-Based Validation

Validators are regular Kotlin functions, enabling natural composition:

```kotlin
// Reusable, parameterized validator
context(_: Validation)
fun validatePrice(value: Double, max: Double = 10000.0): Double {
    return value.ensurePositive().ensureAtMost(max)
}

// Compose validators freely
context(_: Validation)
fun Product.validate() = schema {
    ::price { validatePrice(it) }
    ::discountedPrice { validatePrice(it, max = price) }  // Cross-field reference
}
```

### Konform Equivalent

```kotlin
val validateProduct = Validation<Product> {
    Product::price {
        minimum(0.0)
        maximum(10000.0)
    }
    // Cross-field reference requires additional setup
}
```

## Summary

| Aspect              | Kova                   | Hibernate Validator    | Konform              |
|---------------------|------------------------|------------------------|----------------------|
| Custom validators   | Single function        | Annotation + class     | Lambda               |
| Cross-property      | Direct property access | Class-level annotation | Requires workarounds |
| Argument validation | Built-in (`capture`)   | Not supported          | Not supported        |
| Composition         | Function composition   | Limited                | DSL nesting          |
