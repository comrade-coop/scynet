package com.obecto.trading_bot_breeder

import com.obecto.gattakka.genetics.descriptors._
import spray.json._

object Descriptors {

  /// Utils

  private def makeLayer(name: String, types: List[String], inputs: Int, config: Seq[MapGeneGroupDescriptor.GroupField]): MapGeneGroupDescriptor = {
    MapGeneGroupDescriptor(name + "Layer",
      "type" -> EnumGeneDescriptor(types),
      "inputs" -> GeneGroupDescriptor(List.fill(inputs)(LongGeneDescriptor(8)), ""),
      "config" -> MapGeneGroupDescriptor(config: _*)
    )
  }
  private def makeLayer(_type: String, inputs: Int, config: Seq[MapGeneGroupDescriptor.GroupField]): MapGeneGroupDescriptor = {
    makeLayer(_type, List(_type), inputs, config)
  }
  private def makeInitializerRegularizerConstraint(property: String): Seq[MapGeneGroupDescriptor.GroupField] = {
    Seq(
      s"${property}_regularizer" -> RegularizerDescriptor
      // s"${property}_constraint" -> ConstraintDescriptor,
      // s"${property}_initializer" -> InitializerDescriptor
    )
  }
  private def tupleify(n: Int, descriptor: GeneDescriptor): GeneDescriptor = {
    if (n <= 1) {
      descriptor
    } else {
      GeneGroupDescriptor(List.fill(n)(LongGeneDescriptor(8)))
    }
  }


  /// Helper descriptors

  val ActivationDescriptor = EnumGeneDescriptor("linear", "tanh", "sigmoid", "hard_sigmoid", "elu", "selu", "softplus", "softsign", "softmax")
  val NormalizationDescriptor = MapGeneGroupDescriptor(
    "type" -> EnumGeneDescriptor("mean_stdev", "minmax"),
    "config" -> MapGeneGroupDescriptor(
      "param" -> DoubleGeneDescriptor(0, 1)
    ),
    "window_length" -> LongGeneDescriptor(1, 255)
  )
  val RegularizerDescriptor = MapGeneGroupDescriptor(
    "type" -> EnumGeneDescriptor(List("l1_l2")),
    "config" -> MapGeneGroupDescriptor(
      "l1" -> DoubleGeneDescriptor(0.0, 0.2),
      "l2" -> DoubleGeneDescriptor(0.0, 0.2)
    )
  )
  val ConstraintDescriptor = MapGeneGroupDescriptor(
    "type" -> EnumGeneDescriptor("max_norm", "non_neg", "unit_norm", "min_max_norm"),
    "config" -> MapGeneGroupDescriptor(
      "min" -> DoubleGeneDescriptor(-5.0, 5.0),
      "max" -> DoubleGeneDescriptor(-5.0, 5.0)
    )
  )
  val BooleanDescriptor = EnumGeneDescriptor(true, false)
  val UnitsDescriptor = LongGeneDescriptor(8)
  // val ImageDataFormatDescriptor = EnumGeneDescriptor("channels_first", "channels_last")
  val ImageDataFormatDescriptor = EnumGeneDescriptor(List("channels_last"))


  /// Input Layers

  // val additionalInputSignals = List()
  // val AdditionalInputLayer = MapGeneGroupDescriptor("InputLayer",
  //   "type" -> EnumGeneDescriptor(List("Input")),
  //   "inputs" -> EnumGeneDescriptor(List(List())),
  //   "config" -> EnumGeneDescriptor(inputSignals),
  // )

  val MarketInputLayer = makeLayer("Input", 0, Seq(
    "shape" -> EnumGeneDescriptor(List(List(1))),
    "normalization" -> NormalizationDescriptor,
    "source" -> MapGeneGroupDescriptor(
      "type" -> EnumGeneDescriptor(List("market")),
      "config" -> MapGeneGroupDescriptor(
        "signal" -> EnumGeneDescriptor("close", "open", "high", "low", "volume", "price")
      )
    )
  ))


  /// Common Layers

  // TODO: Reshape, Lambda, RepeatVector?

  val DenseLayer = makeLayer("Dense", 1, Seq(
    "units" -> UnitsDescriptor, // TODO: Extend
    "activation" -> ActivationDescriptor,
    "use_bias" -> BooleanDescriptor,
    "activity_regularizer" -> RegularizerDescriptor
  ) ++
    makeInitializerRegularizerConstraint("kernel") ++
    makeInitializerRegularizerConstraint("bias"))

  val DropoutLayer = makeLayer("Dropout", List("Dropout", "AlphaDropout", "GaussianDropout"), 1, Seq(
    "rate" -> DoubleGeneDescriptor(0, 1)
  ))

  val GaussianNoiseLayer = makeLayer("GaussianNoise", 1, Seq(
    "stddev" -> DoubleGeneDescriptor(0, 6)
  ))

  val FlattenLayer = makeLayer("Flatten", 1, Seq())

  val ActivityRegularizationLayer = makeLayer("ActivityRegularization", 1, Seq(
    "l1" -> DoubleGeneDescriptor(0.0, 0.2),
    "l2" -> DoubleGeneDescriptor(0.0, 0.2)
  ))


  /// Conv Layers

  val ConvBase = Seq(
    "activation" -> ActivationDescriptor,
    "filters" -> LongGeneDescriptor(1, 250),
    "kernel_size" -> LongGeneDescriptor(1, 30), // NOTE: crashy
    "padding" -> EnumGeneDescriptor("valid", "same"),
    // "strides" -> LongGeneDescriptor(0, 3),
    "use_bias" -> BooleanDescriptor,
    "activity_regularizer" -> RegularizerDescriptor
  ) ++
    makeInitializerRegularizerConstraint("kernel") ++
    makeInitializerRegularizerConstraint("bias")

  val Conv1DLayer = makeLayer("Conv1D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(0, 10)
  ))

  val Conv2DLayer = makeLayer("Conv2D", List("Conv2D", "Conv2DTranspose"), 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(0, 10),
    "data_format" -> ImageDataFormatDescriptor
  ))

  val SeparableConv2DLayer = makeLayer("SeparableConv2D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(0, 10),
    "data_format" -> ImageDataFormatDescriptor
  ) ++
    makeInitializerRegularizerConstraint("depthwise") ++
    makeInitializerRegularizerConstraint("pointwise"))

  val Conv3DLayer = makeLayer("Conv3D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(0, 10),
    "data_format" -> ImageDataFormatDescriptor
  ))

  val LocallyConnected1DLayer = makeLayer("LocallyConnected1D", 1, ConvBase ++ Seq())

  val LocallyConnected2DLayer = makeLayer("LocallyConnected2D", 1, ConvBase ++ Seq(
    "data_format" -> ImageDataFormatDescriptor
  ))

  val CroppingLayers = Range.inclusive(1, 3).toList.map(dims => makeLayer(s"Cropping${dims}D", 1, Seq(
      "cropping" -> tupleify(dims, GeneGroupDescriptor(LongGeneDescriptor(0, 16), LongGeneDescriptor(0, 16)))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  ))

  val UpSamplingLayers = Range.inclusive(1, 3).toList.map(dims => makeLayer(s"UpSampling${dims}D", 1, Seq(
      "size" -> tupleify(dims, LongGeneDescriptor(1, 16))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  ))

  val ZeroPaddingLayers = Range.inclusive(1, 3).toList.map(dims => makeLayer(s"ZeroPadding${dims}D", 1, Seq(
      "padding" -> tupleify(dims, GeneGroupDescriptor(LongGeneDescriptor(0, 16), LongGeneDescriptor(0, 16)))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  ))


  /// Pooling Layers

  val PoolingLayers = Range.inclusive(1, 3).toList.map(dims => makeLayer(s"Pooling${dims}D", List(s"MaxPooling${dims}D", s"AveragePooling${dims}D"), 1, Seq(
      "pool_size" -> tupleify(dims, LongGeneDescriptor(0, 16)),
      "padding" -> EnumGeneDescriptor("valid", "same")
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  ))

  val GlobalPooling1DLayer = makeLayer("GlobalPooling1D", List("GlobalMaxPooling1D", "GlobalAveragePooling1D"), 1, Seq())
  val GlobalPooling2DLayer = makeLayer("GlobalPooling2D", List("GlobalMaxPooling2D", "GlobalAveragePooling2D"), 1, Seq(
    "data_format" -> ImageDataFormatDescriptor
  ))

  /// RNN Layers

  val RNNBase = Seq(
    "go_backwards" -> BooleanDescriptor,
    "return_sequences" -> BooleanDescriptor,
    "return_state" -> BooleanDescriptor,
    "stateful" -> BooleanDescriptor,
    "unroll" -> BooleanDescriptor,
    "activity_regularizer" -> RegularizerDescriptor
  ) ++
    makeInitializerRegularizerConstraint("kernel") ++
    makeInitializerRegularizerConstraint("recurrent") ++
    makeInitializerRegularizerConstraint("bias")

  val SimpleRNNLayer = makeLayer("SimpleRNN", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "activation" -> ActivationDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor
  ))

  val GRULayer = makeLayer("GRU", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "activation" -> ActivationDescriptor,
    "recurrent_activation" -> ActivationDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor,
    "reset_after" -> BooleanDescriptor
  ))

  val LSTMLayer = makeLayer("LSTM", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "activation" -> ActivationDescriptor,
    "recurrent_activation" -> ActivationDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor,
    "unit_forget_bias" -> BooleanDescriptor
  ))

  /// Merge Layers

  val MergeOpLayer = makeLayer("Merge", List("Add", "Subtract", "Multiply", "Average", "Maximum"), 2, Seq())

  val ConcatenateLayer = makeLayer("Concatenate", 2, Seq(
    "axis" -> LongGeneDescriptor(1, 3)
  ))

  val DotLayer = makeLayer("Dot", 2, Seq(
    "axes" -> LongGeneDescriptor(1, 3)
  ))

  /// Activation Layers

  val ActivationLayer = makeLayer("Activation", 1, Seq(
    "activation" -> ActivationDescriptor
  ))

  val LeakyReLULayer = makeLayer("LeakyReLU", 1, Seq(
    "alpha" -> DoubleGeneDescriptor(0, 1)
  ))

  val ELULayer = makeLayer("ELU", 1, Seq(
    "alpha" -> DoubleGeneDescriptor(0, 1)
  ))

  val PReLULayer = makeLayer("PReLU", 1, Seq(
  ) ++
    makeInitializerRegularizerConstraint("alpha"))

  val ThresholdedReLU = makeLayer("ELU", 1, Seq(
    "theta" -> DoubleGeneDescriptor(0, 2)
  ))

  /// Configs

  val AdamConfig = MapGeneGroupDescriptor("AdamConfig",
    "optimizer" -> MapGeneGroupDescriptor(
      "type" -> EnumGeneDescriptor(List("Adam")),
      "config" -> MapGeneGroupDescriptor("lr" -> DoubleGeneDescriptor(0, 2))
    ),
    "batch_size" -> EnumGeneDescriptor(List(1)),
    "loss" -> EnumGeneDescriptor(List("mean_squared_error"))
  )

  /// Lists

  val Configs = List[GeneDescriptor](
    AdamConfig
  )

  val InputLayers = List[GeneDescriptor](
    MarketInputLayer
  )

  val Layers = List[GeneDescriptor]( // Some are duplicated to give them additional weigth
    DenseLayer,
    DenseLayer,
    ActivationLayer,
    DropoutLayer,
    GaussianNoiseLayer,
    FlattenLayer,
    MarketInputLayer,
    ActivityRegularizationLayer,
    MergeOpLayer,
    MergeOpLayer,
    MergeOpLayer,
    ConcatenateLayer,
    DotLayer,
    LeakyReLULayer,
    ELULayer,
    PReLULayer,
    ThresholdedReLU,
    Conv1DLayer,
    Conv2DLayer,
    SeparableConv2DLayer,
    Conv3DLayer,
    LocallyConnected1DLayer,
    LocallyConnected2DLayer,
    GlobalPooling1DLayer,
    GlobalPooling2DLayer,
    SimpleRNNLayer,
    GRULayer,
    LSTMLayer
  ) ++
    CroppingLayers ++
    UpSamplingLayers ++
    ZeroPaddingLayers ++
    PoolingLayers

  object AnyJsonProtocol {
    implicit val AnyFormat = new JsonFormat[Any] {
      def write(thing: Any): JsValue = thing match {
        case d: Double => JsNumber(d)
        case l: Long => JsNumber(l)
        case i: Int => JsNumber(i)
        case s: String => JsString(s)
        case b: Boolean => JsBoolean(b)
        case o: Some[_] => write(o.get)
        case null => JsNull
        case None => JsNull
        case m: Map[_, _] => JsObject(m.map(x => (x._1.toString, write(x._2))))
        case a: Seq[_] => JsArray(a.view.map(write(_)).toVector)
        case _ => JsObject()
      }

      def read(value: JsValue): Any = value match {
        case JsNumber(n) => {
          if (n.isValidInt) n.toInt
          else if (n.isValidLong) n.toLong
          else n.toDouble
        }
        case JsString(s) => s
        case JsBoolean(b) => b
        case JsNull => null
        case JsArray(a) => a.map(read(_)).toVector
        case JsObject(o) => o.map(x => (x._1, read(x._2))).toMap
      }
    }
  }
}
