package torture

import scala.collection.mutable.ArrayBuffer
import Rand._

class SeqALU(xregs: HWRegPool, use_mul: Boolean, use_div: Boolean, rvc: Boolean, rvc_bias: Boolean) extends InstSeq //TODO: better configuration
{
  override val seqname = "xalu"
  def seq_immfn(op: Opcode, immfn: () => Int) = () =>
  {
    val dest = reg_write_visible(xregs)
    val imm = Imm(immfn())
    insts += op(dest, imm)
  }
  
  def seq_immfn_not_x2(op: Opcode, immfn: () => Int) = () =>
  {
    val dest = reg_write_visible_not_x2(xregs)
    val imm = Imm(immfn())
    insts += op(dest, imm)
  }

  def seq_immfn_x2(op: Opcode, immfn: () => Int) = () =>
  {
    val dest = reg_write_visible_x2(xregs)
    val imm = Imm(immfn())
    insts += op(dest, imm)
  }

  def seq_immfn_srcx2(op: Opcode, immfn: () => Int) = () =>
  {
    val src = reg_write_hidden_x2(xregs)
    val dest = reg_write_visible_c8(xregs)
    val imm = Imm(immfn())
    insts += op(dest, src, imm)
  }

  def seq_immfn_c8(op: Opcode, immfn: () => Int) = () =>
  {
    val dest = reg_write_visible_c8(xregs)
    val imm = Imm(immfn())
    insts += op(dest, imm)
  }
  
  def seq_immfn_c_bias(op: Opcode, immfn: () => Int) = () => // instr sequence that better fits compressed instructions
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

  def seq_dest1_c(op: Opcode) = () =>
  {
    val src1 = reg_read_any_not_x0(xregs)
    val dest = reg_write(xregs, src1)
    insts += op(dest, src1)
  }

  def seq_dest1_c8(op: Opcode) = () =>
  {
    val src1 = reg_read_any_c8(xregs)
    val dest = reg_write_c8(xregs, src1)
    insts += op(dest, src1)
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
  candidates += seq_src1_immfn(SLLI, rand_shamt)
  candidates += seq_src1_immfn(SLTI, rand_imm) 	 	 // c.slti does not exist
  candidates += seq_src1_immfn(SLTIU, rand_imm)  	 // c.sltiu does not exist
  candidates += seq_src1_immfn(XORI, rand_imm) 	 	 // c.xori does not exist
  candidates += seq_src1_immfn(SRLI, rand_shamt)
  candidates += seq_src1_immfn(SRAI, rand_shamt)
  candidates += seq_src1_immfn(ORI, rand_imm) 	 	 // c.ori does not exist
  candidates += seq_src1_immfn(ANDI, rand_imm)

  val oplist = new ArrayBuffer[Opcode]
//            5    3    x    x     x    3    x    x   3    3	<- number equals register specifier size in rvc version, x equals not existing for rvc.
  oplist += (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)
  if (use_mul) oplist += (MUL, MULH, MULHSU, MULHU)
  if (use_div) oplist += (DIV, DIVU, REM, REMU)

  for (op <- oplist) // mby make onf
  {
    candidates += seq_src1(op)
    candidates += seq_src1_zero(op, rand_imm_c)
    candidates += seq_src2(op)
    candidates += seq_src2_zero(op, rand_imm_c)
    candidates += seq_dest2_c(op)
  }

  if (rvc)
  {
    candidates += seq_immfn_not_x2(C_LUI, rand_immu_c) 
    candidates += seq_immfn(C_LI, rand_nzimm_c) // Change back to only seq_immfn when pysim and rtl allows sp as dest
    candidates += seq_immfn(C_ADDI, rand_nzimm_c)
    candidates += seq_immfn(C_SLLI, rand_shamt_c)
    
    candidates += seq_immfn_c8(C_ANDI, rand_nzimm_c) // Does not say it should be non zero in ISA, but get illegal operand error
    candidates += seq_immfn_c8(C_SRLI, rand_shamt_c)
    candidates += seq_immfn_c8(C_SRAI, rand_shamt_c)
    candidates += seq_dest1_c8(C_SUB)
    candidates += seq_dest1_c8(C_XOR)
    candidates += seq_dest1_c8(C_OR)
    candidates += seq_dest1_c8(C_AND)
    
    candidates += seq_immfn_x2(C_ADDI16SP, rand_nzimm_c_scale16) // got illegal operand error
    candidates += seq_immfn_srcx2(C_ADDI4SPN, rand_bigimm_c_scale4) // got illegal operand error
    
    candidates += seq_dest1_c(C_MV)
    candidates += seq_dest1_c(C_ADD)

    /*
    val oplist_c = new ArrayBuffer[Opcode]
    oplist_c += (C_ADD, C_SUB, C_XOR, C_OR, C_AND)
    for (op <- oplist_c)
    {
      candidates += seq(
    }
    */
  }
  if (rvc_bias)
  {
    candidates += seq_immfn(LUI, rand_immu_c)		 // compressed coverage
    candidates += seq_dest2_immfn_c(ADDI, rand_nzimm_c)	 // compressed coverage
    candidates += seq_dest2_immfn_c(SLLI, rand_shamt_c)	 // compressed coverage 
    candidates += seq_dest2_immfn_c(SRLI, rand_shamt_c)	 // compressed only x8-x15 but increases chance <- made a filter for these registers
    candidates += seq_dest2_immfn_c(SRAI, rand_shamt_c)	 // compressed only x8-x15 but increases chance
    candidates += seq_dest2_immfn_c(ANDI, rand_imm_c)	 // compressed only x8-x15 but increases chance
  }

  rand_pick(candidates)()
}
