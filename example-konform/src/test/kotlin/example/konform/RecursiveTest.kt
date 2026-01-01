package example.konform

import io.konform.validation.constraints.maxItems
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.komapper.extension.validator.Validation
import org.komapper.extension.validator.maxSize
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

        val node by lazy {
            val list = mutableListOf<Node>()
            val n1 = Node(children = list)
            val n2 = Node(children = listOf(n1))
            val n3 = Node(children = listOf(n1, n2))
            list.add(n3)
            n3
        }

        context("konform") {
            // See https://www.konform.io/

            /*
             * Not executed, as it would result in a stack overflow.
             */
            xtest("test") {
                val result = validationNode(node)
                result.isValid shouldBe true
            }
        }

        context("kova") {

            test("valid") {
                fun Validation.validate(node: Node) {
                    node.schema {
                        node::children {
                            maxSize(it, 2)
                        }
                    }
                }

                val result = tryValidate { validate(node) }
                result.shouldBeSuccess()
            }

            test("invalid") {
                fun Validation.validate(node: Node) {
                    node.schema {
                        node::children {
                            maxSize(it, 1)
                        }
                    }
                }

                val result = tryValidate { validate(node) }
                result.shouldBeFailure()
                result.messages.size shouldBe 1
                result.messages[0].text shouldBe "Collection (size 2) must have at most 1 elements"
                result.messages[0].root shouldBe "example.konform.Node"
                result.messages[0].path.fullName shouldBe "children"
            }
        }
    })
