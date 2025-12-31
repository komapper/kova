package example.hibernate.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.tryValidate
import java.util.Locale

class ClassLevelConstraintTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("hibernate-validator") {
            // See https://github.com/hibernate/hibernate-validator/blob/9.1/documentation/src/test/java/org/hibernate/validator/referenceguide/chapter06/classlevel/ClassLevelConstraintTest.java
        }

        context("kova") {
            class Person(
                val name: String,
            )

            class Car(
                val seatCount: Int,
                val passengers: List<Person>,
            )

            fun Validation.validate(car: Car) =
                car.schema {
                    car.constrain("validPassengerCount") {
                        satisfies(it.passengers.size <= it.seatCount) {
                            text("There must be not more passengers than seats.")
                        }
                    }
                }

            test("testClassLevelConstraint") {
                val car =
                    Car(
                        2,
                        listOf(
                            Person("Alice"),
                            Person("Bob"),
                            Person("Bill"),
                        ),
                    )

                val result = tryValidate { validate(car) }

                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "There must be not more passengers than seats."
            }
        }
    })
