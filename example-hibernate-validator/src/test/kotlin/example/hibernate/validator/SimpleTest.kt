package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureAtLeast
import org.komapper.extension.validator.ensureLengthInRange
import org.komapper.extension.validator.ensureNotNull
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import jakarta.validation.Validation as HibernateValidation

class SimpleTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("hibernate-validator") {
            // See https://github.com/hibernate/hibernate-validator/blob/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide/chapter01/CarTest.java

            class Car(
                @field:NotNull
                val manufacturer: String?,
                @field:Size(min = 2, max = 14)
                @field:NotNull
                val licensePlate: String?,
                @field:Min(2)
                val seatCount: Int,
            )

            val factory: ValidatorFactory = HibernateValidation.buildDefaultValidatorFactory()
            val validator = factory.validator

            test("manufacturerIsNull") {
                val car = Car(null, "DD-AB-123", 4)
                val constraintViolations =
                    validator.validate(car)

                constraintViolations.size shouldBe 1
                constraintViolations.iterator().next().message shouldBe "must not be null"
            }

            test("licensePlateTooShort") {
                val car = Car("Morris", "D", 4)

                val constraintViolations = validator.validate(car)

                constraintViolations.size shouldBe 1
                constraintViolations.iterator().next().message shouldBe "size must be between 2 and 14"
            }

            test("seatCountTooLow") {
                val car = Car("Morris", "DD-AB-123", 1)

                val constraintViolations = validator.validate(car)

                constraintViolations.size shouldBe 1
                constraintViolations.iterator().next().message shouldBe "must be greater than or equal to 2"
            }

            test("carIsValid") {
                val car = Car("Morris", "DD-AB-123", 2)

                val constraintViolations = validator.validate(car)

                constraintViolations.size shouldBe 0
            }
        }

        context("kova") {
            class Car(
                val manufacturer: String?,
                val licensePlate: String?,
                val seatCount: Int,
            )

            context(_: Validation)
            fun Car.validate() =
                schema {
                    ::manufacturer { it.ensureNotNull() }
                    ::licensePlate {
                        it.ensureNotNull()
                        it.ensureLengthInRange(2..14)
                    }
                    ::seatCount { it.ensureAtLeast(2) }
                }

            test("manufacturerIsNull") {
                val car = Car(null, "DD-AB-123", 4)

                val result = tryValidate { car.validate() }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
            }

            test("licensePlateTooShort") {
                val car = Car("Morris", "D", 4)

                val result = tryValidate { car.validate() }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must have length within range 2..14"
            }

            test("seatCountTooLow") {
                val car = Car("Morris", "DD-AB-123", 1)

                val result = tryValidate { car.validate() }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be greater than or equal to 2"
            }

            test("carIsValid") {
                val car = Car("Morris", "DD-AB-123", 2)

                val result = tryValidate { car.validate() }

                result.shouldBeSuccess()
            }
        }
    })
