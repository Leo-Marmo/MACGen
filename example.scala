class Sha3Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
    opcodes = opcodes, nPTWPorts = if (p(Sha3TLB).isDefined) 1 else 0) {
  override lazy val module = new Sha3AccelImp(this)
  val dmemOpt = p(Sha3TLB).map { _ =>
    val dmem = LazyModule(new DmemModule)
    tlNode := dmem.node
    dmem
  }
}

class Sha3AccelImp(outer: Sha3Accel)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {
  // Suppress DCE to ensure that the module ports are kept consistent
  // between the regular generated Verilog and Sha3BlackBox version
  chisel3.dontTouch(io)

  //parameters
  val W = p(Sha3WidthP)
  val S = p(Sha3Stages)
  //constants
  val r = 2*256
  val c = 25*W - r
  val round_size_words = c/W
  val rounds = 24 //12 + 2l
  val hash_size_words = 256/W
  val bytes_per_word = W/8

  //RoCC Interface defined in testMems.scala
  //cmd
  //resp
  io.resp.valid := Bool(false) //Sha3 never returns values with the resp
  //mem
  //busy
  if (p(Sha3BlackBox)) {
    require(!p(Sha3TLB).isDefined, "Blackbox SHA3 does not support Dmemmodule")

    val sha3bb = Module(new Sha3BlackBox)
    io <> sha3bb.io.io
    sha3bb.io.clock := clock
    sha3bb.io.reset := reset
  } else {

    val ctrl = Module(new CtrlModule(W,S)(p))

    ctrl.io.rocc_req_val   <> io.cmd.valid
    io.cmd.ready := ctrl.io.rocc_req_rdy
    ctrl.io.rocc_funct     <> io.cmd.bits.inst.funct
    ctrl.io.rocc_rs1       <> io.cmd.bits.rs1
    ctrl.io.rocc_rs2       <> io.cmd.bits.rs2
    ctrl.io.rocc_rd        <> io.cmd.bits.inst.rd
    io.busy := ctrl.io.busy

    val status = RegEnable(io.cmd.bits.status, io.cmd.fire())
    val dmem_data = Wire(Bits())
    def dmem_ctrl(req: DecoupledIO[HellaCacheReq]) {
      req.valid := ctrl.io.dmem_req_val
      ctrl.io.dmem_req_rdy := req.ready
      req.bits.tag := ctrl.io.dmem_req_tag
      req.bits.addr := ctrl.io.dmem_req_addr
      req.bits.cmd := ctrl.io.dmem_req_cmd
      req.bits.size := ctrl.io.dmem_req_size
      req.bits.data := dmem_data
      req.bits.signed := Bool(false)
      req.bits.dprv := status.dprv
      req.bits.dv := status.dv
      req.bits.phys := Bool(false)
    }

    outer.dmemOpt match {
      case Some(m) => {
        val dmem = m.module
        dmem_ctrl(dmem.io.req)
        io.mem.req <> dmem.io.mem
        io.ptw.head <> dmem.io.ptw

        dmem.io.status := status
        dmem.io.sfence := ctrl.io.sfence
      }
      case None => dmem_ctrl(io.mem.req)
    }

    ctrl.io.dmem_resp_val  <> io.mem.resp.valid
    ctrl.io.dmem_resp_tag  <> io.mem.resp.bits.tag
    ctrl.io.dmem_resp_data := io.mem.resp.bits.data

    val dpath = Module(new DpathModule(W,S)(p))

    dpath.io.message_in <> ctrl.io.buffer_out
    dmem_data := dpath.io.hash_out(ctrl.io.windex)

    //ctrl.io <> dpath.io
    dpath.io.absorb := ctrl.io.absorb
    dpath.io.init := ctrl.io.init
    dpath.io.write := ctrl.io.write
    dpath.io.round := ctrl.io.round
    dpath.io.stage := ctrl.io.stage
    dpath.io.aindex := ctrl.io.aindex
  }
}