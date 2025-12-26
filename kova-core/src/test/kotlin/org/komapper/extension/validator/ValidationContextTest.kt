package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

data class TestData(
    val value: String,
)

@IgnorableReturnValue
context(actual: T)
fun <T> contextShouldBe(expected: T) = actual shouldBe expected

class ValidationContextTest :
    FunSpec({

        context("addRoot") {
            test("no root") {
                context(Validation(path = Path("c", null, null))) {
                    addRoot("a", null) {
                        contextShouldBe(Validation("a", Path("", null, null)))
                    }
                }
            }

            test("already has root") {
                context(Validation(root = "a", path = Path("c", null, null))) {
                    addRoot("b", null) {
                        contextShouldBe(Validation("a", Path("c", null, null)))
                    }
                }
            }

            test("add empty") {
                context(Validation(path = Path("c", null, null))) {
                    addRoot("", null) {
                        contextShouldBe(Validation("", Path("", null, null)))
                    }
                }
            }
        }

        context("addPath") {
            test("no path") {
                context(Validation("a")) {
                    addPath("b", null) {
                        contextShouldBe(Validation("a", Path("b", null, Path("", null, null))))
                    }
                }
            }

            test("already has path") {
                context(Validation("a", Path("b", null, null))) {
                    addPath("c", null) {
                        contextShouldBe(Validation("a", Path("c", null, Path("b", null, null))))
                    }
                }
            }

            test("add empty") {
                context(Validation("a")) {
                    addPath("", null) {
                        contextShouldBe(Validation("a", Path("", null, Path("", null, null))))
                    }
                }
            }
        }

        context("addPathChecked") {
            test("detect circular reference - direct") {
                val obj = object {}
                context(Validation("a", Path("b", obj, null))) {
                    addPathChecked("c", obj) { error("unreachable") }.shouldBeNull()
                }
            }

            test("detect circular reference - nested") {
                val obj1 = object {}
                val obj2 = object {}
                val grandparent = Path("level1", obj1, null)
                val parent = Path("level2", obj2, grandparent)
                context(Validation("a", parent)) {
                    addPathChecked("level3", obj1) { error("unreachable") }.shouldBeNull()
                }
            }

            test("no circular reference with different objects") {
                val obj1 = object {}
                val obj2 = object {}
                context(Validation("a", Path("b", obj1, null))) {
                    addPathChecked("c", obj2) {}.shouldNotBeNull()
                }
            }

            test("no circular reference with null objects") {
                context(Validation("a", Path("b", null, null))) {
                    addPathChecked("c", null) {}.shouldNotBeNull()
                }
            }

            test("allow same value but different object instances") {
                // String interning might make these the same reference, so use objects instead
                val data1 = TestData("test")
                val data2 = TestData("test")
                context(Validation("a", Path("b", data1, null))) {
                    addPathChecked("c", data2) {}.shouldNotBeNull()
                }
            }
        }

        context("appendPath") {
            test("no path") {
                context(Validation("a")) {
                    appendPath("b") { contextShouldBe(Validation("a", Path("b", null, null))) }
                }
            }

            test("already has path") {
                context(Validation("a", Path("b", null, null))) {
                    appendPath("c") { contextShouldBe(Validation("a", Path("bc", null, null))) }
                }
            }

            test("add empty") {
                context(Validation("a")) {
                    appendPath("") { contextShouldBe(Validation("a", Path("", null, null))) }
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
