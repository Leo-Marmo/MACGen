// https://github.com/chipsalliance/rocket-chip/blob/master/src/main/scala/tile/LazyRoCC.scala
class MACAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes){
  override lazy val module = new Sha3AccelImp(this)
}

class MACAccelImp(outer: MACAccel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
    // Suppress DCE to ensure that the module ports are kept consistent
    // between the regular generated Verilog and Sha3BlackBox version
    chisel3.dontTouch(io)

    //parameters
    val W = 4.U

    //RoCC Interface defined in testMems.scala
    //cmd
    //resp
    io.resp.valid := Bool(true) //Sha3 never returns values with the resp

    val dpath = Module(new MACGen(W)(p))
    io <> ctrl.io
}