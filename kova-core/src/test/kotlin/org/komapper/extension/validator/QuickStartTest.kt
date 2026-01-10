package org.komapper.extension.validator

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class QuickStartTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("basic") {
            context(_: Validation)
            fun validateProductName(name: String): String {
                name.ensureNotBlank()
                name.ensureLengthInRange(1..100)
                return name
            }

            test("tryValidate") {
                val result = tryValidate { validateProductName("Wireless Mouse") }
                if (result.isSuccess()) {
                    println("Valid: ${result.value}") // Valid: Wireless Mouse
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeSuccess()
            }

            test("validate") {
                try {
                    val value = validate { validateProductName("Wireless Mouse") }
                    println("Valid: $value") // Valid: Wireless Mouse
                } catch (e: ValidationException) {
                    e.messages.forEach { println("Invalid: $it") }

                    fail("never reach here")
                }
            }
        }

        context("multiple") {
            context(_: Validation)
            fun validateProductName(name: String): String {
                name.ensureNotBlank()
                name.ensureLengthInRange(1..100)
                return name
            }

            context(_: Validation)
            fun validatePrice(price: Double): Double {
                price.ensureInClosedRange(0.0..1000.0)
                return price
            }

            test("success") {
                val result =
                    tryValidate {
                        val name = validateProductName("Wireless Mouse")
                        val price = validatePrice(29.99)
                        name to price
                    }

                if (result.isSuccess()) {
                    println("Valid: ${result.value}") // Valid: (Wireless Mouse, 29.99)
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeSuccess()
            }

            test("failure") {
                val result =
                    tryValidate {
                        val name = validateProductName("Wireless Mouse")
                        val price = validatePrice(1029.99)
                        name to price
                    }

                if (result.isSuccess()) {
                    println("Valid: ${result.value}")
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeFailure()
            }
        }

        context("object") {
            data class Product(
                val id: Int,
                val name: String,
                val price: Double,
            )

            context(_: Validation)
            fun Product.validate() =
                schema {
                    ::id { it.ensureAtLeast(1) }
                    ::name {
                        it.ensureNotBlank()
                        it.ensureLengthAtLeast(1)
                        it.ensureLengthAtMost(100)
                    }
                    ::price { it.ensureAtLeast(0.0) }
                }

            test("test") {
                val result = tryValidate { Product(1, "Mouse", 29.99).validate() }
                result.shouldBeSuccess()
            }
        }

        context("nested object") {
            data class Address(
                val street: String,
                val city: String,
                val zipCode: String,
            )

            data class Customer(
                val name: String,
                val email: String,
                val address: Address,
            )

            context(_: Validation)
            fun Address.validate() =
                schema {
                    ::street {
                        it.ensureNotBlank()
                        it.ensureLengthAtLeast(1)
                    }
                    ::city {
                        it.ensureNotBlank()
                        it.ensureLengthAtLeast(1)
                    }
                    ::zipCode { it.ensureMatches(Regex("^\\d{5}(-\\d{4})?$")) }
                }

            context(_: Validation)
            fun Customer.validate() =
                schema {
                    ::name {
                        it.ensureNotBlank()
                        it.ensureLengthAtLeast(1)
                        it.ensureLengthAtMost(100)
                    }
                    ::email {
                        it.ensureNotBlank()
                        it.ensureContains("@")
                    }
                    ::address { it.validate() } // Nested validation
                }

            test("test") {
                val customer =
                    Customer(
                        name = "John Doe",
                        email = "invalid-email",
                        address = Address(street = "", city = "Tokyo", zipCode = "123"),
                    )

                val result = tryValidate { customer.validate() }

                if (result.isSuccess()) {
                    println("Valid")
                } else {
                    println("Invalid")
                    result.messages.joinToString("\n").let { println(it) }
                    // Message(constraintId=kova.charSequence.ensureContains, text='must contain "@"', root=Customer, path=email, input=invalid-email, args=[@])
                    // Message(constraintId=kova.charSequence.ensureNotBlank, text='must not be ensureBlank', root=Customer, path=address.street, input=, args=[])
                    // Message(constraintId=kova.charSequence.lengthAtLeast, text='must be at least 1 characters', root=Customer, path=address.street, input=, args=[1])
                    // Message(constraintId=kova.charSequence.ensureMatches, text='must match pattern: ^\d{5}(-\d{4})?$', root=Customer, path=address.zipCode, input=123, args=[^\d{5}(-\d{4})?$])
                }

                result.shouldBeFailure()
            }
        }

        context("cross-property ") {
            data class PriceRange(
                val minPrice: Double,
                val maxPrice: Double,
            )

            context(_: Validation)
            fun PriceRange.validate() =
                schema {
                    ::minPrice { it.ensureNotNegative() }
                    ::maxPrice { it.ensureNotNegative() }

                    // Validate relationship
                    constrain("priceRange") {
                        satisfies(it.minPrice <= it.maxPrice) {
                            text("minPrice must be less than or equal to maxPrice")
                        }
                    }
                }

            test("test") {
                val result = tryValidate { PriceRange(10.0, 100.0).validate() }

                result.shouldBeSuccess()
            }
        }

        context("fail fast") {
            context(_: Validation)
            fun validateProductName(name: String) {
                name.ensureNotBlank()
                name.ensureLengthInRange(1..100)
            }

            test("test") {
                val result =
                    tryValidate(ValidationConfig(failFast = true)) {
                        validateProductName("Wireless Mouse")
                    }

                result.shouldBeSuccess()
            }
        }

        context("custom clock") {
            context(_: Validation)
            fun validateDate(date: LocalDate) {
                date.ensureFuture()
            }

            test("test") {
                val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))
                val result =
                    tryValidate(config = ValidationConfig(clock = fixedClock)) {
                        val date = LocalDate.of(2024, 6, 20)
                        date.ensureFuture() // Uses the fixed clock for comparison
                    }

                result.shouldBeSuccess()
            }
        }

        context("debug logging") {
            context(_: Validation)
            fun validateUsername(username: String) {
                username.ensureLengthAtLeast(3)
                username.ensureLengthAtMost(20)
            }

            test("test") {
                val result =
                    tryValidate(
                        config =
                            ValidationConfig(
                                logger = { logEntry -> println("[Validation] $logEntry") },
                            ),
                    ) {
                        validateUsername("ab")
                    }

                result.shouldBeFailure()
            }
        }
    })
