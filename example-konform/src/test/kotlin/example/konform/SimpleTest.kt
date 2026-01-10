package example.konform

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureInRange
import org.komapper.extension.validator.ensureMaxLength
import org.komapper.extension.validator.ensureMinLength
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

class SimpleTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        data class UserProfile(
            val fullName: String,
            val age: Int?,
        )

        val validUserProfile = UserProfile("Alice", 21)
        val invalidUserProfile = UserProfile("B", 200)

        context("konform") {
            // See https://www.konform.io/

            val validateUser =
                KonformValidation {
                    UserProfile::fullName {
                        minLength(2)
                        maxLength(100)
                    }
                    UserProfile::age ifPresent {
                        minimum(0)
                        maximum(150)
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
                    it.message shouldBe "must have at least 2 characters"
                    it.dataPath shouldBe ".fullName"
                }
                result.errors[1].let {
                    it.message shouldBe "must be at most '150'"
                    it.dataPath shouldBe ".age"
                }
            }
        }

        context("kova") {
            context(_: Validation)
            fun validate(userProfile: UserProfile) =
                userProfile.schema {
                    userProfile::fullName {
                        it.ensureMinLength(2)
                        it.ensureMaxLength(100)
                    }
                    userProfile::age {
                        if (it != null) it.ensureInRange(0..150)
                    }
                }

            test("valid") {
                val result = tryValidate { validate(validUserProfile) }
                result.shouldBeSuccess()
            }

            test("invalid") {
                val result = tryValidate { validate(invalidUserProfile) }
                result.shouldBeFailure()
                result.messages.size shouldBe 2
                result.messages[0].let {
                    it.text shouldBe "must be at least 2 characters"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "fullName"
                }
                result.messages[1].let {
                    it.text shouldBe "must be within range 0..150"
                    it.root shouldBe "UserProfile"
                    it.path.fullName shouldBe "age"
                }
            }
        }
    })
