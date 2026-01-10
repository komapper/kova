package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureAtLeast
import org.komapper.extension.validator.ensureEquals
import org.komapper.extension.validator.ensureLengthAtLeast
import org.komapper.extension.validator.ensureLengthAtMost
import org.komapper.extension.validator.ensureNotNull
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.text
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

            context(_: Validation)
            fun validate(
                person: Person,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = person.schema {
                if (Check.DEFAULT in checks) {
                    person::name { it.ensureNotNull() }
                }
            }

            context(_: Validation)
            fun validate(
                driver: Driver,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = driver.schema {
                validate(driver as Person, checks)
                if (Check.DRIVER in checks) {
                    driver::age {
                        it.ensureAtLeast(18) { text("You have to be 18 to drive a car") }
                    }
                    driver::hasDriverLicense {
                        it.ensureEquals(true) {
                            text("You first have to pass the driving test")
                        }
                    }
                }
            }

            context(_: Validation)
            fun validate(
                car: Car,
                checks: Set<Check> = setOf(Check.DEFAULT),
            ) = car.schema {
                if (Check.DEFAULT in checks) {
                    car::manufacturer {
                        it.ensureNotNull()
                    }
                    car::licencePlate {
                        it.ensureNotNull()
                        it.ensureLengthAtLeast(2)
                        it.ensureLengthAtMost(14)
                    }
                    car::seatCount {
                        it.ensureAtLeast(2)
                    }
                }

                if (Check.CAR in checks) {
                    car::passedVehicleInspection {
                        it.ensureEquals(true) {
                            text("The car ensureHas to pass the vehicle inspection first")
                        }
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

                // but ensureHas it passed the vehicle inspection?
                result = tryValidate { validate(car, setOf(Check.CAR)) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "The car ensureHas to pass the vehicle inspection first"

                // let's go to the vehicle inspection
                car.passedVehicleInspection = true
                result = tryValidate { validate(car, setOf(Check.CAR)) }
                result.shouldBeSuccess()

                // now let's add a driver. He is 18, but ensureHas not passed the driving test yet
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
