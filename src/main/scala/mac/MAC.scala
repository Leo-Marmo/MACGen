package mac
import chisel3._

class MACGen(width: Int) extends Module {

    val io = IO(new Bundle {
        val num1 = Input(UInt(width.W))
        val num2 = Input(UInt(width.W))
        val out  = Output(UInt((2 * width).W))
    })
  
    val accum = RegInit(0.U(12.W))

    // Next posedge clk value
    val nextAccum = io.num1 * io.num2 + accum
    accum := nextAccum
    
    // Setup out output
    io.out := accum
}

class CustomAccelerator(opcodes: OpcodeSet)
    (implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new CustomAcceleratorModule(this)
}

class CustomAcceleratorModule(outer: CustomAccelerator)
    extends LazyRoCCModuleImp(outer) {
  val cmd = Queue(io.cmd)
  // The parts of the command are as follows
  // inst - the parts of the instruction itself
  //   opcode
  //   rd - destination register number
  //   rs1 - first source register number
  //   rs2 - second source register number
  //   funct
  //   xd - is the destination register being used?
  //   xs1 - is the first source register being used?
  //   xs2 - is the second source register being used?
  // rs1 - the value of source register 1
  // rs2 - the value of source register 2
  ...
}