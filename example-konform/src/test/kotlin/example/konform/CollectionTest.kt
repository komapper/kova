package example.konform

import io.konform.validation.constraints.maxItems
import io.konform.validation.constraints.minItems
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import io.konform.validation.constraints.pattern
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.matches
import org.komapper.extension.validator.maxSize
import org.komapper.extension.validator.minLength
import org.komapper.extension.validator.minSize
import org.komapper.extension.validator.minValue
import org.komapper.extension.validator.notNull
import org.komapper.extension.validator.onEach
import org.komapper.extension.validator.onEachValue
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

class CollectionTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        data class Person(
            val name: String,
            val email: String?,
            val age: Int,
        )

        data class Event(
            val organizer: Person,
            val attendees: List<Person>,
            val ticketPrices: Map<String, Double?>,
        )

        val validEvent =
            Event(
                organizer = Person("Alice", "alice@@bigcorp.com", 25),
                attendees = listOf(Person("Bob", "bob@@example.com", 18), Person("Charlie", null, 25)),
                ticketPrices = mapOf("VIP" to 100.0, "Regular" to 10.0),
            )

        val invalidEvent =
            Event(
                organizer = Person("Alice", null, 25),
                attendees = listOf(Person("Bob", "example@example", 18), Person("Charlie", null, 16)),
                ticketPrices = mapOf("VIP" to 100.0, "Regular" to 0.0),
            )

        context("konform") {
            // See https://www.konform.io/

            val validateEvent =
                KonformValidation {
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

            test("success") {
                val result = validateEvent(validEvent)
                result.isValid shouldBe true
            }

            test("failure") {
                val result = validateEvent(invalidEvent)
                result.isValid shouldBe false
                result.errors.size shouldBe 4
                result.errors[0].let {
                    it.message shouldBe "Email address must be given"
                    it.dataPath shouldBe ".organizer.email"
                }
                result.errors[1].let {
                    it.message shouldBe "Please provide a valid email address (optional)"
                    it.dataPath shouldBe ".attendees[0].email"
                }
                result.errors[2].let {
                    it.message shouldBe "Attendees must be 18 years or older"
                    it.dataPath shouldBe ".attendees[1].age"
                }
                result.errors[3].let {
                    it.message shouldBe "must be at least '0.01'"
                    it.dataPath shouldBe ".ticketPrices.Regular"
                }
            }
        }

        context("kova") {
            fun Validation.validateOrganizer(person: Person) =
                person.schema {
                    person::email {
                        notNull(it) { text("Email address must be given") }
                        matches(it, Regex(".+@bigcorp.com")) { text("Organizers must have a BigCorp email address") }
                    }
                }

            fun Validation.validateAttendee(person: Person) =
                person.schema {
                    person::name { minLength(it, 2) }
                    person::age { minValue(it, 18) { text("Attendees must be 18 years or older") } }
                    person::email {
                        if (it != null) {
                            matches(
                                it,
                                Regex(".+@.+\\..+"),
                            ) { text("Please provide a valid email address (optional)") }
                        }
                    }
                }

            fun Validation.validate(event: Event) =
                event.schema {
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

            test("valid") {
                val result = tryValidate { validate(validEvent) }
                result.shouldBeSuccess()
            }

            test("invalid") {
                val result = tryValidate { validate(invalidEvent) }
                result.shouldBeFailure()
                result.messages.size shouldBe 3
                result.messages[0].let {
                    it.text shouldBe "Email address must be given"
                    it.root shouldBe "Event"
                    it.path.fullName shouldBe "organizer.email"
                }
                result.messages[1].let {
                    it.text shouldBe
                        "Some elements do not satisfy the constraint: [Please provide a valid email address (optional), Attendees must be 18 years or older]"
                    it.root shouldBe "Event"
                    it.path.fullName shouldBe "attendees"
                    val descendants = it.descendants
                    descendants.size shouldBe 2
                    descendants[0].let {
                        it.text shouldBe "Please provide a valid email address (optional)"
                        it.root shouldBe "Event"
                        it.path.fullName shouldBe "attendees[0]<iterable element>.email"
                    }
                    descendants[1].let {
                        it.text shouldBe "Attendees must be 18 years or older"
                        it.root shouldBe "Event"
                        it.path.fullName shouldBe "attendees[1]<iterable element>.age"
                    }
                }
                result.messages[2].let {
                    it.text shouldBe "Some values do not satisfy the constraint: [must be greater than or equal to 0.01]"
                    it.root shouldBe "Event"
                    it.path.fullName shouldBe "ticketPrices"
                    val descendants = it.descendants
                    descendants.size shouldBe 1
                    descendants[0].let {
                        it.text shouldBe "must be greater than or equal to 0.01"
                        it.root shouldBe "Event"
                        it.path.fullName shouldBe "ticketPrices[Regular]<map value>"
                    }
                }
            }
        }
    })
