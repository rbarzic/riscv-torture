package torture

class Inst(opcode: String, val operands: Array[Operand])
{
  def is_branch = List("beq", "bne", "blt", "bge", "bltu", "bgeu").contains(opcode)

  def is_jalr = List("jalr", "jalr.c", "jalr.j", "jalr.r").contains(opcode)

  def is_la = opcode == "la"

  override def toString = opcode + operands.mkString(" ", ", ", "")
}

class Opcode(name: String)
{
  def apply(opnds: Operand*) = new Inst(name, opnds.toArray)
}

object J extends Opcode("j")
object JAL extends Opcode("jal")
object BEQ extends Opcode("beq")
object BNE extends Opcode("bne")
object BLT extends Opcode("blt")
object BGE extends Opcode("bge")
object BLTU extends Opcode("bltu")
object BGEU extends Opcode("bgeu")
object JALR extends Opcode("jalr")
object JALR_C extends Opcode("jalr.c")
object JALR_R extends Opcode("jalr.r")
object JALR_J extends Opcode("jalr.j")
object RDNPC extends Opcode("rdnpc")

object LA extends Opcode("la")
object LB extends Opcode("lb")
object LH extends Opcode("lh")
object LW extends Opcode("lw")
object LD extends Opcode("ld")
object LBU extends Opcode("lbu")
object LHU extends Opcode("lhu")
object LWU extends Opcode("lwu")
object SB extends Opcode("sb")
object SH extends Opcode("sh")
object SW extends Opcode("sw")
object SD extends Opcode("sd")

object AMOADD_W extends Opcode("amoadd.w")
object AMOSWAP_W extends Opcode("amoswap.w")
object AMOAND_W extends Opcode("amoand.w")
object AMOOR_W extends Opcode("amoor.w")
object AMOMIN_W extends Opcode("amomin.w")
object AMOMINU_W extends Opcode("amominu.w")
object AMOMAX_W extends Opcode("amomax.w")
object AMOMAXU_W extends Opcode("amomaxu.w")
object AMOADD_D extends Opcode("amoadd.d")
object AMOSWAP_D extends Opcode("amoswap.d")
object AMOAND_D extends Opcode("amoand.d")
object AMOOR_D extends Opcode("amoor.d")
object AMOMIN_D extends Opcode("amomin.d")
object AMOMAX_D extends Opcode("amomax.d")
object AMOMAXU_D extends Opcode("amomaxu.d")

object ADDI extends Opcode("addi")
object SLLI extends Opcode("slli")
object SLTI extends Opcode("slti")
object SLTIU extends Opcode("sltiu")
object XORI extends Opcode("xori")
object SRLI extends Opcode("srli")
object SRAI extends Opcode("srai")
object ORI extends Opcode("ori")
object ANDI extends Opcode("andi")
object ADD extends Opcode("add")
object SUB extends Opcode("sub")
object SLL extends Opcode("sll")
object SLT extends Opcode("slt")
object SLTU extends Opcode("sltu")
object XOR extends Opcode("xor")
object SRL extends Opcode("srl")
object SRA extends Opcode("sra")
object OR extends Opcode("or")
object AND extends Opcode("and")
object MUL extends Opcode("mul")
object MULH extends Opcode("mulh")
object MULHSU extends Opcode("mulhsu")
object MULHU extends Opcode("mulhu")
object DIV extends Opcode("div")
object DIVU extends Opcode("divu")
object REM extends Opcode("rem")
object REMU extends Opcode("remu")
object LUI extends Opcode("lui")

object ADDIW extends Opcode("addiw")
object SLLIW extends Opcode("slliw")
object SRLIW extends Opcode("srliw")
object SRAIW extends Opcode("sraiw")
object ADDW extends Opcode("addw")
object SUBW extends Opcode("subw")
object SLLW extends Opcode("sllw")
object SRLW extends Opcode("srlw")
object SRAW extends Opcode("sraw")
object MULW extends Opcode("mulw")
object DIVW extends Opcode("divw")
object DIVUW extends Opcode("divuw")
object REMW extends Opcode("remw")
object REMUW extends Opcode("remuw")

object FLW extends Opcode("flw")
object FLD extends Opcode("fld")
object FSW extends Opcode("fsw")
object FSD extends Opcode("fsd")

object FADD_S extends Opcode("fadd.s")
object FSUB_S extends Opcode("fsub.s")
object FMUL_S extends Opcode("fmul.s")
object FDIV_S extends Opcode("fdiv.s")
object FSQRT_S extends Opcode("fsqrt.s")
object FMIN_S extends Opcode("fmin.s")
object FMAX_S extends Opcode("fmax.s")
object FADD_D extends Opcode("fadd.d")
object FSUB_D extends Opcode("fsub.d")
object FMUL_D extends Opcode("fmul.d")
object FDIV_D extends Opcode("fdiv.d")
object FSQRT_D extends Opcode("fsqrt.d")
object FMIN_D extends Opcode("fmin.d")
object FMAX_D extends Opcode("fmax.d")
object FMADD_S extends Opcode("fmadd.s")
object FMSUB_S extends Opcode("fmsub.s")
object FNMSUB_S extends Opcode("fnmsub.s")
object FNMADD_S extends Opcode("fnmadd.s")
object FMADD_D extends Opcode("fmadd.d")
object FMSUB_D extends Opcode("fmsub.d")
object FNMSUB_D extends Opcode("fnmsub.d")
object FNMADD_D extends Opcode("fnmadd.d")
object FSGNJ_S extends Opcode("fsgnj.s")
object FSGNJN_S extends Opcode("fsgnjn.s")
object FSGNJX_S extends Opcode("fsgnjx.s")
object FSGNJ_D extends Opcode("fsgnj.d")
object FSGNJN_D extends Opcode("fsgnjn.d")
object FSGNJX_D extends Opcode("fsgnjx.d")
object FCVT_S_D extends Opcode("fcvt.s.d")
object FCVT_D_S extends Opcode("fcvt.d.s")
object FCVT_S_L extends Opcode("fcvt.s.l")
object FCVT_S_LU extends Opcode("fcvt.s.lu")
object FCVT_S_W extends Opcode("fcvt.s.w")
object FCVT_S_WU extends Opcode("fcvt.s.wu")
object FCVT_D_L extends Opcode("fcvt.d.l")
object FCVT_D_LU extends Opcode("fcvt.d.lu")
object FCVT_D_W extends Opcode("fcvt.d.w")
object FCVT_D_WU extends Opcode("fcvt.d.wu")
object MXTF_S extends Opcode("mxtf.s")
object MXTF_D extends Opcode("mxtf.d")
object MTFSR extends Opcode("mtfsr")
object FCVT_L_S extends Opcode("fcvt.l.s")
object FCVT_LU_S extends Opcode("fcvt.lu.s")
object FCVT_W_S extends Opcode("fcvt.w.s")
object FCVT_WU_S extends Opcode("fcvt.wu.s")
object FCVT_L_D extends Opcode("fcvt.l.d")
object FCVT_LU_D extends Opcode("fcvt.lu.d")
object FCVT_W_D extends Opcode("fcvt.w.d")
object FCVT_WU_D extends Opcode("fcvt.wu.d")
object MFTX_S extends Opcode("mftx.s")
object MFTX_D extends Opcode("mftx.d")
object MFFSR extends Opcode("mffsr")
object FEQ_S extends Opcode("feq.s")
object FLT_S extends Opcode("flt.s")
object FLE_S extends Opcode("fle.s")
object FEQ_D extends Opcode("feq.d")
object FLT_D extends Opcode("flt.d")
object FLE_D extends Opcode("fle.d")
object FENCE_I extends Opcode("fence.i")
object FENCE extends Opcode("fence")
object SYSCALL extends Opcode("syscall")
object BREAK extends Opcode("break")
object RDCYCLE extends Opcode("rdcycle")
object RDTIME extends Opcode("rdtime")
object RDINSTRET extends Opcode("rdinstret")

object NOP extends Opcode("nop")
object LI extends Opcode("li")
object MFPCR extends Opcode("mfpcr")
object MTPCR extends Opcode("mtpcr")
object VVCFGIVL extends Opcode("vvcfgivl")
object VLD extends Opcode("vld")
object VF extends Opcode("vf")
object VFSD extends Opcode("vfsd")
object STOP extends Opcode("stop")
object VMSV extends Opcode("vmsv")
object VSETVL extends Opcode("vsetvl")
object VFSW extends Opcode("vfsw")
object UTIDX extends Opcode("utidx")
object VSD extends Opcode("vsd")
object VSW extends Opcode("vsw")
object VLW extends Opcode("vlw")
object VFLD extends Opcode("vfld")
object VMVV extends Opcode("vmvv")
object VFLW extends Opcode("vflw")
object VFMVV extends Opcode("vfmvv")
object MOVZ extends Opcode("movz")
object MOVN extends Opcode("movn")
object FMOVZ extends Opcode("fmovz")
object FMOVN extends Opcode("fmovn")
object AMOMINU_D extends Opcode("amominu.d")
object FENCE_V_G extends Opcode("fence.v.g")
object FENCE_V_L extends Opcode("fence.v.l")

object ILLEGAL extends Opcode(".word")
