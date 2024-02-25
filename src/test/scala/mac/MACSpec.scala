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
class MACSpec extends AnyFreeSpec with ChiselScalatestTester {

  "Adding product of 1x1 and 2x1 should yield 3" in {
    test(new MACGen(4)) { c =>
    
        //Setup our inputs to be 1 and 1
      c.io.num1.poke(1.U)     // Set our input to value 1
      c.io.num2.poke(1.U)     // Set our input to value 1
      c.clock.step(1)
      c.io.out.expect(1.U)

        //Setup our inputs to be 2 and 1
      c.io.num1.poke(2.U)     // Set our input to value 0
      c.io.num2.poke(1.U)     // Set our input to value 0
      c.clock.step(1)
      c.io.out.expect(3.U)
  }
  }
}
