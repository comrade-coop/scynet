package ai.scynet

import java.math.BigInteger

import ai.scynet.messages.{Block, Trace, Transaction}
import com.sksamuel.avro4s.{FromValue, RecordFormat, ToSchema, ToValue}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8

//
object implicits {
  implicit def int2BigInteger( i : Int): BigInteger = {
    return BigInteger.valueOf(i)
  }


  implicit object BigIntegerToSchema extends ToSchema[BigInteger] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }

  implicit object DateTimeToValue extends ToValue[BigInteger] {
    override def apply(value: BigInteger): String = value.toString
  }

  implicit object DateTimeFromValue extends FromValue[BigInteger] {
    override def apply(value: Any, field: Field): BigInteger = new BigInteger( value.asInstanceOf[Utf8].toString )
  }

}

