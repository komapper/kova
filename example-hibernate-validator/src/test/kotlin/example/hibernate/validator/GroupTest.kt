package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.eq
import org.komapper.extension.validator.maxLength
import org.komapper.extension.validator.min
import org.komapper.extension.validator.minLength
import org.komapper.extension.validator.notNull
import org.komapper.extension.validator.toNonNullable
import org.komapper.extension.validator.tryValidate
import java.util.Locale

class GroupTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("hibernate-validator") {
            // See https://github.com/hibernate/hibernate-validator/blob/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide/chapter05/GroupTest.java
        }

        context("kova") {
            open class Person(
                val name: String?,
            )

            class Driver(
                name: String?,
            ) : Person(name) {
                var age: Int = 0
                var hasDriverLicense: Boolean = false

                fun passedDrivingTest(hasDriverLicense: Boolean) {
                    this.hasDriverLicense = hasDriverLicense
                }
            }

            class Car(
                val manufacturer: String?,
                val licencePlate: String?,
                val seatCount: Int,
            ) {
                var driver: Driver? = null
                var passedVehicleInspection: Boolean = false
            }

            fun Validation.validate(
                person: Person,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = person.schema {
                if (Check.DEFAULT in checks) {
                    person::name { notNull(it) }
                }
            }

            fun Validation.validate(
                driver: Driver,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = driver.schema {
                validate(driver as Person, checks)
                if (Check.DRIVER in checks) {
                    driver::age {
                        min(it, 18) { text("You have to be 18 to drive a car") }
                    }
                    driver::hasDriverLicense {
                        eq(it, true) {
                            text("You first have to pass the driving test")
                        }
                    }
                }
            }

            fun Validation.validate(
                car: Car,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = car.schema {
                if (Check.DEFAULT in checks) {
                    car::manufacturer {
                        notNull(it)
                    }
                    car::licencePlate {
                        val v = toNonNullable(it)
                        minLength(v, 2)
                        maxLength(v, 14)
                    }
                    car::seatCount {
                        min(it, 2)
                    }
                }

                if (Check.CAR in checks) {
                    car::passedVehicleInspection {
                        eq(
                            it,
                            true,
                        ) { text("The car has to pass the vehicle inspection first") }
                    }
                }

                car::driver {
                    if (it != null) validate(it, checks)
                }
            }

            test("driveAway") {
                // create a car and check that everything is ok with it.
                val car = Car("Morris", "DD-AB-123", 2)
                var result = tryValidate { validate(car) }
                result.shouldBeSuccess()

                // but has it passed the vehicle inspection?
                result = tryValidate { validate(car, setOf(Check.CAR)) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "The car has to pass the vehicle inspection first"

                // let's go to the vehicle inspection
                car.passedVehicleInspection = true
                result = tryValidate { validate(car, setOf(Check.CAR)) }
                result.shouldBeSuccess()

                // now let's add a driver. He is 18, but has not passed the driving test yet
                val john = Driver("John Doe")
                john.age = 18
                car.driver = john
                result = tryValidate { validate(car, setOf(Check.DRIVER)) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "You first have to pass the driving test"

                // ok, John passes the test
                john.passedDrivingTest(true)
                result = tryValidate { validate(car) }
                result.shouldBeSuccess()

                // just checking that everything is in order now
                result = tryValidate { validate(car, setOf(Check.DEFAULT, Check.CAR, Check.DRIVER)) }
                result.shouldBeSuccess()
            }
        }
    }) {
    enum class Check {
        DEFAULT,
        CAR,
        DRIVER,
    }
}
