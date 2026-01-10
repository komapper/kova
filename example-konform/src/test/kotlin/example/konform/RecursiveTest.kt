package example.konform

import io.konform.validation.constraints.maxItems
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.ensureEach
import org.komapper.extension.validator.ensureSizeAtMost
import org.komapper.extension.validator.schema
import org.komapper.extension.validator.tryValidate
import java.util.Locale
import io.konform.validation.Validation as KonformValidation

data class Node(
    val children: List<Node>,
)

val validationNode =
    KonformValidation {
        Node::children {
            maxItems(2)
        }
        Node::children onEach {
            runDynamic { validationRef }
        }
    }

private val validationRef get(): KonformValidation<Node> = validationNode

class RecursiveTest :
    FunSpec({
        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        val validNode by lazy {
            val n1 = Node(children = emptyList())
            val n2 = Node(children = listOf(n1))
            val n3 = Node(children = listOf(n1, n2))
            n3
        }

        val invalidNode by lazy {
            val n1 = Node(children = emptyList())
            val n2 = Node(children = listOf(n1))
            val n3 = Node(children = listOf(n1, n2))
            val n4 = Node(children = listOf(n1, n2, n3))
            n4
        }

        val cyclicNode by lazy {
            val list = mutableListOf<Node>()
            val n1 = Node(children = list)
            val n2 = Node(children = listOf(n1))
            val n3 = Node(children = listOf(n1, n2))
            list.add(n3)
            n3
        }

        context("konform") {
            // See https://www.konform.io/

            test("valid") {
                val result = validationNode(validNode)
                result.isValid shouldBe true
            }

            test("invalid") {
                val result = validationNode(invalidNode)
                result.isValid shouldBe false
                result.errors.size shouldBe 1
                result.errors[0].message shouldBe "must have at most 2 items"
                result.errors[0].dataPath shouldBe ".children"
            }

            /**
             * Not executed, as it would result in a stack overflow.
             */
            xtest("cyclic") {
                val result = validationNode(cyclicNode)
                result.isValid shouldBe true
            }
        }

        context("kova") {

            context(_: Validation)
            fun validate(node: Node) {
                node.schema {
                    node::children { children ->
                        children.ensureSizeAtMost(2)
                        children.ensureEach { validate(it) }
                    }
                }
            }

            test("valid") {
                val result = tryValidate { validate(validNode) }
                result.shouldBeSuccess()
            }

            test("invalid") {
                val result = tryValidate { validate(invalidNode) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "Collection (size 3) must have at most 2 elements"
                result.messages[0].root shouldBe "example.konform.Node"
                result.messages[0].path.fullName shouldBe "children"
            }

            test("cyclic") {
                val result = tryValidate { validate(cyclicNode) }
                result.shouldBeSuccess()
            }
        }
    })
