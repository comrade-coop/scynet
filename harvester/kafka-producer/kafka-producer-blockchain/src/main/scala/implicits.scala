package ai.scynet
import java.math.BigInteger

import ai.scynet.messages.{Block, Trace, Transaction}
import com.sksamuel.avro4s.{FromValue, RecordFormat, ToSchema, ToValue}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field
import org.apache.avro.generic.GenericRecord
import shapeless.Poly1
import shapeless.ops.product.ToRecord
import org.web3j.protocol.core.DefaultBlockParameter
import shapeless.Coproduct
//
object implicits {
  implicit def int2BigInteger( i : Int): BigInteger = {
    return BigInteger.valueOf(i)
  }

  implicit def int2DefaultBlockParameter( i : Int): DefaultBlockParameter = {
    return DefaultBlockParameter.valueOf(i)
  }

  implicit def BigInteger2DefaultBlockParameter( i : BigInteger): DefaultBlockParameter = {
    return DefaultBlockParameter.valueOf(i)
  }
//
  val format = RecordFormat[Transaction]
  implicit def Transaction2GenericRecord(t: Transaction): GenericRecord = {
    format.to(t)
  }

  val formatTrace = RecordFormat[Trace]
  implicit def Trace2GenericRecord(t: Trace): GenericRecord = {
    formatTrace.to(t)
  }

  val formatBlock = RecordFormat[Block]
  implicit def Block2GenericRecord(t: Block): GenericRecord = {
    formatBlock.to(t)
  }


  implicit def Biginteger2BigDecimal(bigInteger: BigInteger) : BigDecimal = {
    return BigDecimal(bigInteger)
  }

  implicit object BigIntegerToSchema extends ToSchema[BigInteger] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object DateTimeToValue extends ToValue[BigInteger] {
    override def apply(value: BigInteger): String = value.toString
  }

  implicit object DateTimeFromValue extends FromValue[BigInteger] {
    override def apply(value: Any, field: Field): BigInteger = new BigInteger( value.asInstanceOf[String] )
  }

}

