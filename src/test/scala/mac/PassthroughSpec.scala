// See README.md for license details.

package mac

import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import chisel3.experimental.BundleLiterals._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDSpec
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDSpec'
  * }}}
  * Testing from mill:
  * {{{
  * mill %NAME%.test.testOnly gcd.GCDSpec
  * }}}
  */
class PassthroughSpec extends AnyFreeSpec with ChiselScalatestTester {

  "Input should equal output" in {
    test(new PassthroughGen(4)) { c =>
      c.io.in.poke(0.U)     // Set our input to value 0
      c.io.out.expect(0.U)  // Assert that the output correctly has 0
      c.io.in.poke(1.U)     // Set our input to value 1
      c.io.out.expect(1.U)  // Assert that the output correctly has 1
      c.io.in.poke(2.U)     // Set our input to value 2
      c.io.out.expect(2.U)  // Assert that the output correctly has 2
  }
  }
}
