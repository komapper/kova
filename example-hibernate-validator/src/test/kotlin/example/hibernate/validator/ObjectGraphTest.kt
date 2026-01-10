package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.Valid
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureNotNull
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import jakarta.validation.Validation as HibernateValidation

class ObjectGraphTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("hibernate-validator") {
            // See https://github.com/hibernate/hibernate-validator/tree/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide/chapter02/objectgraph

            class Person(
                @field:NotNull
                val name: String?,
            )

            class Car(
                @field:NotNull
                @field:Valid
                val driver: Person?,
            )

            val factory: ValidatorFactory = HibernateValidation.buildDefaultValidatorFactory()
            val validator = factory.validator

            test("driverIsNull") {
                val car = Car(null)
                val constraintViolations =
                    validator.validate(car)

                constraintViolations.size shouldBe 1
                val violation = constraintViolations.iterator().next()
                violation.message shouldBe "must not be null"
                val iterator = violation.propertyPath.iterator()
                iterator.next().name shouldBe "driver"
            }

            test("driverNameIsNull") {
                val car = Car(Person(null))

                val constraintViolations = validator.validate(car)

                constraintViolations.size shouldBe 1
                val violation = constraintViolations.iterator().next()
                violation.message shouldBe "must not be null"
                val iterator = violation.propertyPath.iterator()
                iterator.next().name shouldBe "driver"
                iterator.next().name shouldBe "name"
            }

            test("carIsValid") {
                val car = Car(Person("Smith"))

                val constraintViolations = validator.validate(car)

                constraintViolations.size shouldBe 0
            }
        }

        context("kova") {
            class Person(
                val name: String?,
            )

            class Car(
                val driver: Person?,
            )

            context(_: Validation)
            fun validate(person: Person) =
                person.schema {
                    person::name {
                        ensureNotNull(it)
                    }
                }

            context(_: Validation)
            fun validate(car: Car) =
                car.schema {
                    car::driver {
                        ensureNotNull(it)
                        validate(it)
                    }
                }

            test("driverIsNull") {
                val car = Car(null)

                val result = tryValidate { validate(car) }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
                result.messages[0].path.fullName shouldBe "driver"
            }

            test("driverNameIsNull") {
                val car = Car(Person(null))

                val result = tryValidate { validate(car) }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "must not be null"
                result.messages[0].path.fullName shouldBe "driver.name"
            }

            test("carIsValid") {
                val car = Car(Person("Smith"))

                val result = tryValidate { validate(car) }

                result.shouldBeSuccess()
            }
        }
    })
