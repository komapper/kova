package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.maxLength
import org.komapper.extension.validator.minLength
import org.komapper.extension.validator.notNull
import org.komapper.extension.validator.tryValidate
import org.komapper.extension.validator.uppercase
import java.util.Locale

class ConstraintCompositionTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("hibernate-validator") {
            // See https://github.com/hibernate/hibernate-validator/tree/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide/chapter06/constraintcomposition
        }

        context("kova") {
            class Car(
                val licensePlate: String?,
            )

            fun Validation.validateLicensePlate(s: String?) {
                notNull(s)
                minLength(s, 2)
                maxLength(s, 14)
                uppercase(s)
            }

            fun Validation.validate(car: Car) =
                car.schema {
                    car::licensePlate {
                        validateLicensePlate(it)
                    }
                }

            test("testClassLevelConstraint") {
                val car = Car("dd-ab-123")

                val result = tryValidate { validate(car) }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must be uppercase"
            }

            test("carIsValid") {
                val car = Car("DD-AB-123")

                val result = tryValidate { validate(car) }

                result.shouldBeSuccess()
            }
        }
    })
