package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

data class TestData(
    val value: String,
)

class ValidationContextTest :
    FunSpec({

        context("addRoot") {
            test("no root") {
                val context = ValidationContext(path = Path("c", null, null))
                context.addRoot("a", null) {
                    shouldBe(ValidationContext("a", Path("", null, null)))
                }
            }

            test("already has root") {
                val context = ValidationContext(root = "a", path = Path("c", null, null))
                context.addRoot("b", null) {
                    shouldBe(ValidationContext("a", Path("c", null, null)))
                }
            }

            test("add empty") {
                val context = ValidationContext(path = Path("c", null, null))
                context.addRoot("", null) {
                    shouldBe(ValidationContext("", Path("", null, null)))
                }
            }
        }

        context("addPath") {
            test("no path") {
                val context = ValidationContext("a")
                context.addPath("b", null) {
                    shouldBe(ValidationContext("a", Path("b", null, Path("", null, null))))
                }
            }

            test("already has path") {
                val context = ValidationContext("a", Path("b", null, null))
                context.addPath("c", null) {
                    shouldBe(ValidationContext("a", Path("c", null, Path("b", null, null))))
                }
            }

            test("add empty") {
                val context = ValidationContext("a")
                context.addPath("", null) {
                    shouldBe(ValidationContext("a", Path("", null, Path("", null, null))))
                }
            }
        }

        context("addPathChecked") {
            test("detect circular reference - direct") {
                val obj = object {}
                val context = ValidationContext("a", Path("b", obj, null))
                context.addPathChecked("c", obj) { error("unreachable") }.shouldBeNull()
            }

            test("detect circular reference - nested") {
                val obj1 = object {}
                val obj2 = object {}
                val obj3 = object {}
                val grandparent = Path("level1", obj1, null)
                val parent = Path("level2", obj2, grandparent)
                val context = ValidationContext("a", parent)
                context.addPathChecked("level3", obj1) { error("unreachable") }.shouldBeNull()
            }

            test("no circular reference with different objects") {
                val obj1 = object {}
                val obj2 = object {}
                val context = ValidationContext("a", Path("b", obj1, null))
                context.addPathChecked("c", obj2) {}.shouldNotBeNull()
            }

            test("no circular reference with null objects") {
                val context = ValidationContext("a", Path("b", null, null))
                context.addPathChecked("c", null) {}.shouldNotBeNull()
            }

            test("allow same value but different object instances") {
                val obj1 = "test"
                val obj2 = "test"
                // String interning might make these the same reference, so use objects instead
                val data1 = TestData("test")
                val data2 = TestData("test")
                val context = ValidationContext("a", Path("b", data1, null))
                context.addPathChecked("c", data2) {}.shouldNotBeNull()
            }
        }

        context("appendPath") {
            test("no path") {
                val context = ValidationContext("a")
                context.appendPath("b") {
                    shouldBe(ValidationContext("a", Path("b", null, null)))
                }
            }

            test("already has path") {
                val context = ValidationContext("a", Path("b", null, null))
                context.appendPath("c") {
                    shouldBe(ValidationContext("a", Path("bc", null, null)))
                }
            }

            test("add empty") {
                val context = ValidationContext("a")
                context.appendPath("") {
                    shouldBe(ValidationContext("a", Path("", null, null)))
                }
            }
        }

        context("Path.fullName") {
            test("single path with no parent") {
                val path = Path("user", null, null)
                path.fullName shouldBe "user"
            }

            test("nested path with one parent") {
                val parent = Path("user", null, null)
                val path = Path("name", null, parent)
                path.fullName shouldBe "user.name"
            }

            test("deeply nested path") {
                val grandparent = Path("company", null, null)
                val parent = Path("user", null, grandparent)
                val path = Path("name", null, parent)
                path.fullName shouldBe "company.user.name"
            }

            test("empty name with no parent") {
                val path = Path("", null, null)
                path.fullName shouldBe ""
            }

            test("empty name with parent") {
                val parent = Path("user", null, null)
                val path = Path("", null, parent)
                path.fullName shouldBe "user"
            }

            test("parent with empty name") {
                val parent = Path("", null, null)
                val path = Path("name", null, parent)
                path.fullName shouldBe "name"
            }

            test("multiple levels with empty names") {
                val grandparent = Path("", null, null)
                val parent = Path("user", null, grandparent)
                val path = Path("name", null, parent)
                path.fullName shouldBe "user.name"
            }

            test("empty name in the middle") {
                val grandparent = Path("company", null, null)
                val parent = Path("", null, grandparent)
                val path = Path("name", null, parent)
                // When parent name is empty, it's treated as "no parent"
                // so grandparent is ignored
                path.fullName shouldBe "name"
            }
        }

        context("Path.containsObject") {
            test("contains object in current path") {
                val obj = object {}
                val path = Path("test", obj, null)
                path.containsObject(obj) shouldBe true
            }

            test("contains object in parent path") {
                val obj1 = object {}
                val obj2 = object {}
                val parent = Path("parent", obj1, null)
                val path = Path("child", obj2, parent)
                path.containsObject(obj1) shouldBe true
            }

            test("contains object in grandparent path") {
                val obj1 = object {}
                val obj2 = object {}
                val obj3 = object {}
                val grandparent = Path("grandparent", obj1, null)
                val parent = Path("parent", obj2, grandparent)
                val path = Path("child", obj3, parent)
                path.containsObject(obj1) shouldBe true
            }

            test("does not contain different object") {
                val obj1 = object {}
                val obj2 = object {}
                val path = Path("test", obj1, null)
                path.containsObject(obj2) shouldBe false
            }

            test("does not contain object when path has null object") {
                val obj = object {}
                val path = Path("test", null, null)
                path.containsObject(obj) shouldBe false
            }

            test("does not contain object in deeply nested path") {
                val obj1 = object {}
                val obj2 = object {}
                val obj3 = object {}
                val objNotInPath = object {}
                val grandparent = Path("grandparent", obj1, null)
                val parent = Path("parent", obj2, grandparent)
                val path = Path("child", obj3, parent)
                path.containsObject(objNotInPath) shouldBe false
            }
        }
    })
