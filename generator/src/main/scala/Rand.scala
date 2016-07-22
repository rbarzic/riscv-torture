package torture

import scala.util.Random
import scala.collection.mutable.ArrayBuffer

object Rand
{
  def rand_word: Int = Random.nextInt
  def rand_dword: Long = Random.nextLong

  def rand_range(low: Int, high: Int): Int =
  {
    var span = high - low + 1
    if (low > high) span = low - high + 1
    low + Random.nextInt(span)
  }

  def rand_shamt() = rand_range(0, 31)
  def rand_shamt_c() = rand_range(1, 31) // shift amount must be non-zero
  def rand_shamtw() = rand_range(0, 31)
  def rand_seglen() = rand_range(0, 7)

  def rand_imm() = rand_range(-2048, 2047)
/*  {	
    if (rvc_bias) {
      if (rand_range(0,1) > 0) {
      	rand_range(-2048, 2047)
      } else {
      	rand_range(-32, 31) // 50% chance of getting within compressed range 	
      }
    } else {
      rand_range(-2048, 2047)
    }
  }
*/  
  def rand_bigimm() = rand_range(0, 1048575)
  def rand_imm_c() = rand_range(-32, 31) // Find out at which range RVC instructions are used C.ADDI has 6 bit imm
  def rand_nzimm_c(): Int  =
  {
    var tmp = rand_range(-32, 30)
    if (tmp >= 0) tmp += 1
    tmp
  }
  def rand_nzimm_c_scale16(): Int  =
  {
    var tmp = rand_range(-32, 30)
    if (tmp >= 0) tmp += 1
    tmp*16
  }  
  def rand_immu_c() = rand_range(1, 31)
  def rand_zimmu_c() = rand_range(0, 63)
  def rand_zimmu_c_scale4() = rand_range(0, 63)*4
  def rand_zimmu5_c_scale4() = rand_range(0, 31)*4
  def rand_bigimm_c() = rand_range(1, 255) // saw some instr type had 8 bit imm? only C.ADDI4SPN it seems
  def rand_bigimm_c_scale4() = rand_range(1, 255)*4
  
  def rand_addr_b(memsize: Int) = rand_range(0, memsize-1)
  def rand_addr_h(memsize: Int) = rand_range(0, memsize-1) & ~1
  def rand_addr_w(memsize: Int) = rand_range(0, memsize-1) & ~3
  def rand_addr_d(memsize: Int) = rand_range(0, memsize-1) & ~7

  def rand_filter(rand: () => Int, filter: (Int) => Boolean) =
  {
    var res = rand()
    while (!filter(res)) res = rand()
    res
  }

  def rand_pick[T](array: ArrayBuffer[T]) =
  {
    array(rand_range(0, array.length-1))
  }

  def rand_permute[T](array: ArrayBuffer[T]) =
  {
    for (i <- 0 to array.length-1)
    {
      val j = rand_range(0, array.length-1)
      val t = array(i)
      array(i) = array(j)
      array(j) = t
    }
  }

  def rand_biased: Long =
  {
    val value = rand_dword
    val s = rand_range(0, 17)

    if (s < 9)
    {
      val small = rand_range(0, 9).toLong

      s match
      {
        // return a value with a single bit set
        case 0 => (1 << value & 63)
        case 1 => (1 << value & 63)
        // return a valueue with a single bit clear
        case 2 => ~(1 << value & 63)
        case 3 => ~(1 << value & 63)
        // return a small integer around zero
        case 4 => small
        // return a very large/very small 8b signed number
        case 5 => ((0x80L + small) << 56) >> 56
        // return a very large/very small 16b signed number
        case 6 => ((0x8000L + small) << 48) >> 48
        // return a very large/very small 32b signed number
        case 7 => ((0x80000000L + small) << 32) >> 32
        // return a very large/very small 64b signed number
        case 8 => 0x800000000000000L + small
      }
    }
    else
    {
      value
    }
  }
}
