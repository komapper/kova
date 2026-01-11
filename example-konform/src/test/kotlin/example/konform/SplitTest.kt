package example.konform

import io.konform.validation.constraints.minimum
import io.konform.validation.required
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureAtLeast
import org.komapper.extension.validator.ensureNotNull
import org.komapper.extension.validator.named
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

class SplitTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        data class UserProfile(
            val fullName: String,
            val age: Int?,
        )

        val validUserProfile = UserProfile("Alice", 51)
        val invalidUserProfile = UserProfile("B", 20)

        context("konform") {
            // See https://www.konform.io/

            val ageCheck =
                KonformValidation<Int?> {
                    required {
                        minimum(21)
                    }
                }

            val validateUser =
                KonformValidation {
                    UserProfile::age {
                        run(ageCheck)
                    }

                    validate("ageMinus10", { it.age?.let { age -> age - 10 } }) {
                        run(ageCheck)
                    }
                }

            test("valid") {
                val result = validateUser(validUserProfile)
                result.isValid shouldBe true
            }

            test("invalid") {
                val result = validateUser(invalidUserProfile)
                result.isValid shouldBe false
                result.errors.size shouldBe 2
                result.errors[0].let {
                    it.message shouldBe "must be at least '21'"
                    it.dataPath shouldBe ".age"
                }
                result.errors[1].let {
                    it.message shouldBe "must be at least '21'"
                    it.dataPath shouldBe ".ageMinus10"
                }
            }
        }

        context("kova") {

            context(_: Validation)
            fun checkAge(age: Int?) {
                age.ensureNotNull().ensureAtLeast(21)
            }

            context(_: Validation)
            fun UserProfile.validate() =
                schema {
                    ::age { checkAge(it) }
                    age.named("ageMinus10") { checkAge(it?.let { age -> age - 10 }) }
                }

            test("valid") {
                val result = tryValidate { validUserProfile.validate() }
                result.shouldBeSuccess()
            }

            test("invalid") {
                val result = tryValidate { invalidUserProfile.validate() }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.text shouldBe "must be greater than or equal to 21"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "age"
                    it.input shouldBe 20
                }
                result.messages[1].let {
                    it.text shouldBe "must be greater than or equal to 21"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "ageMinus10"
                    it.input shouldBe 10
                }
            }
        }
    })
