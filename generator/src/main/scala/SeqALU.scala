package torture

import scala.collection.mutable.ArrayBuffer
import Rand._

class SeqALU(xregs: HWRegPool, use_mul: Boolean, use_div: Boolean, rvc_bias: Boolean) extends InstSeq //TODO: better configuration
{
  override val seqname = "xalu"
  def seq_immfn(op: Opcode, immfn: () => Int) = () =>
  {
    val dest = reg_write_visible(xregs)
    val imm = Imm(immfn())
    insts += op(dest, imm)
  }

  def seq_immfn_c(op: Opcode, immfn: () => Int) = () => // instr sequence that better fits compressed instructions
  {
    val dest = reg_write_visible(xregs)
    val imm = Imm(immfn())
    insts += op(dest, dest, imm)
  }

  def seq_src1(op: Opcode) = () =>
  {
    val src1 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1)
    insts += op(dest, src1, src1)
  }

  def seq_src1_immfn(op: Opcode, immfn: () => Int) = () =>
  {
    val src1 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1)
    val imm = Imm(immfn())
    insts += op(dest, src1, imm)
  }

  def seq_src1_zero(op: Opcode, immfn: () => Int) = () =>
  {
    val src1 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1)
    val tmp = reg_write_visible(xregs)
    insts += ADDI(tmp, reg_read_zero(xregs), Imm(immfn())) // fjerna rand_imm() her
    insts += op(dest, tmp, tmp)
  }

  def seq_src2(op: Opcode) = () =>
  {
    val src1 = reg_read_any(xregs)
    val src2 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1, src2)
    insts += op(dest, src1, src2)
  }

  def seq_src2_zero(op: Opcode, immfn: () => Int) = () =>
  {
    val src1 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1)
    val tmp1 = reg_write_visible(xregs)
    val tmp2 = reg_write_visible(xregs)
    insts += ADDI(tmp1, reg_read_zero(xregs), Imm(immfn())) // HER Å
    insts += ADDI(tmp2, reg_read_zero(xregs), Imm(immfn())) // HER Å
    insts += op(dest, tmp1, tmp2)
  }

  def seq_dest2_immfn_c(op: Opcode, immfn: () => Int) = () => // instr sequence that better fits compressed instructions
  {
    val dest = reg_write_visible(xregs)
    val imm = Imm(immfn())
    insts += op(dest, dest, imm)
  }

  def seq_dest2_c(op: Opcode) = () =>
  {
    val src1 = reg_read_any(xregs)
    val dest = reg_write(xregs, src1)
    insts += op(dest, dest, src1)
  }

  val candidates = new ArrayBuffer[() => insts.type]

  candidates += seq_immfn(LUI, rand_bigimm)
  candidates += seq_src1_immfn(ADDI, rand_imm)
  candidates += seq_dest2_immfn_c(ADDI, rand_imm_c)	 // compressed
  candidates += seq_src1_immfn(SLLI, rand_shamt)
  candidates += seq_dest2_immfn_c(SLLI, rand_shamt)	 // compressed
  candidates += seq_src1_immfn(SLTI, rand_imm) 	 	 // c.slti does not exist
  candidates += seq_src1_immfn(SLTIU, rand_imm)  	 // c.sltiu does not exist
  candidates += seq_src1_immfn(XORI, rand_imm) 	 	 // c.xori does not exist
  candidates += seq_src1_immfn(SRLI, rand_shamt)
  candidates += seq_dest2_immfn_c(SRLI, rand_shamt)	 // compressed only x8-x15 but increases chance
  candidates += seq_src1_immfn(SRAI, rand_shamt)
  candidates += seq_dest2_immfn_c(SRAI, rand_shamt)	 // compressed only x8-x15 but increases chance
  candidates += seq_src1_immfn(ORI, rand_imm) 	 	 // c.ori does not exist
  candidates += seq_src1_immfn(ANDI, rand_imm)
  candidates += seq_dest2_immfn_c(ANDI, rand_imm_c)	 // not guaranteed compressed instr, since C.ANDI requires registers x8-x15 need to find a way to bias/prioritize these

  val oplist = new ArrayBuffer[Opcode]
//            5    3    x    x     x    3    x    x   3    3
  oplist += (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)
  if (use_mul) oplist += (MUL, MULH, MULHSU, MULHU)
  if (use_div) oplist += (DIV, DIVU, REM, REMU)

  for (op <- oplist)
  {
    candidates += seq_src1(op)
    candidates += seq_src1_zero(op, rand_imm_c)
    candidates += seq_src2(op)
    candidates += seq_src2_zero(op, rand_imm_c)
    candidates += seq_dest2_c(op)
  }
  
  rand_pick(candidates)()
}
