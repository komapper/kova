package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidationContextTest :
    FunSpec({

        context("addRoot") {
            test("no root") {
                val context = ValidationContext(path = Path("c", null, null))
                val newContext = context.addRoot(root = "a")
                newContext shouldBe ValidationContext("a", Path("c", null, null))
            }

            test("already has root") {
                val context = ValidationContext(root = "a", path = Path("c", null, null))
                val newContext = context.addRoot("b")
                newContext shouldBe ValidationContext("a", Path("c", null, null))
            }

            test("add empty") {
                val context = ValidationContext(path = Path("c", null, null))
                val newContext = context.addRoot(root = "")
                newContext shouldBe ValidationContext("", Path("c", null, null))
            }
        }

        context("addPath") {
            test("no path") {
                val context = ValidationContext("a")
                val newContext = context.addPath("b", null)
                newContext shouldBe ValidationContext("a", Path("b", null, Path("", null, null)))
            }

            test("already has path") {
                val context = ValidationContext("a", Path("b", null, null))
                val newContext = context.addPath("c", null)
                newContext shouldBe ValidationContext("a", Path("c", null, Path("b", null, null)))
            }

            test("add empty") {
                val context = ValidationContext("a")
                val newContext = context.addPath("", null)
                newContext shouldBe ValidationContext("a", Path("", null, Path("", null, null)))
            }
        }

        context("appendPath") {
            test("no path") {
                val context = ValidationContext("a")
                val newContext = context.appendPath("b")
                newContext shouldBe ValidationContext("a", Path("b", null, null))
            }

            test("already has path") {
                val context = ValidationContext("a", Path("b", null, null))
                val newContext = context.appendPath("c")
                newContext shouldBe ValidationContext("a", Path("bc", null, null))
            }

            test("add empty") {
                val context = ValidationContext("a")
                val newContext = context.appendPath("")
                newContext shouldBe ValidationContext("a", Path("", null, null))
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
    })
