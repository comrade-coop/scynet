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
  private def makeInputLayer(name: String, shape: List[Int], signalType: String, config: GeneDescriptor): MapGeneGroupDescriptor = {
    makeLayer(name, List("Input"), 0, Seq(
      "shape" -> EnumGeneDescriptor(List(shape)),
      "source" -> MapGeneGroupDescriptor(
        "type" -> EnumGeneDescriptor(List(signalType)),
        "config" -> config,
        "preprocessor" -> PreprocessorDescriptor
      )
    ))
  }
  private def makeInitializerRegularizerConstraint(property: String): Seq[MapGeneGroupDescriptor.GroupField] = {
    Seq(
      s"${property}_regularizer" -> RegularizerDescriptor,
      s"${property}_constraint" -> ConstraintDescriptor,
      s"${property}_initializer" -> InitializerDescriptor
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

  val ActivationDescriptor = EnumGeneDescriptor("linear", "tanh", "sigmoid", "hard_sigmoid", "relu", "elu", "selu", "softplus", "softsign", "softmax")
  val PreprocessorDescriptor = MapGeneGroupDescriptor(
    "type" -> EnumGeneDescriptor("RawPreprocessor", "MeanStdevPreprocessor"),
    "config" -> MapGeneGroupDescriptor(
      "preprocess_window_length" -> LongGeneDescriptor(1, 200),
      "normalization_constant" -> DoubleGeneDescriptor(0.1, 0.9)
    )
  )
  val RegularizerDescriptor = MapGeneGroupDescriptor(
    "type" -> EnumGeneDescriptor(List("l1_l2")),
    "config" -> MapGeneGroupDescriptor(
      "l1" -> DoubleGeneDescriptor(0.0, 0.2),
      "l2" -> DoubleGeneDescriptor(0.0, 0.2)
    )
  )
  val ConstraintDescriptor = EnumGeneDescriptor(
    null,
    null,
    null,
    null, // Duplicated to make it more likely to be used
    "max_norm",
    "non_neg",
    "unit_norm",
    "min_max_norm"
  )
  val InitializerDescriptor = EnumGeneDescriptor(
    "zero",
    "one",
    "constant",
    "uniform",
    "normal",
    "truncated_normal",
    "glorot_uniform",
    "glorot_normal",
    "he_uniform",
    "he_normal",
    "lecun_uniform",
    "lecun_normal"
  )
  // val InitializerDescriptorMatrix = EnumGeneDescriptor(InitializerDescriptor.values ++ List(
  //   "identity",
  //   "orthogonal"
  // ))
  val BooleanDescriptor = EnumGeneDescriptor(true, false)
  val UnitsDescriptor = LongGeneDescriptor(1, 600)
  // val ImageDataFormatDescriptor = EnumGeneDescriptor("channels_first", "channels_last")
  val ImageDataFormatDescriptor = EnumGeneDescriptor(List("channels_last"))


  /// Input Layers

  import DefaultJsonProtocol._
  import Converter.AnyJsonProtocol._
  val repositories_text = scala.io.Source.fromFile("../repositories.json").mkString
  val repositories = repositories_text.asJson.convertTo[Map[Any, Any]]


  val InputLayers = repositories.view.flatMap(repository => {
    repository._2.asInstanceOf[Map[Any, Any]].toList.map(source => {
      val source_config = source._2.asInstanceOf[Map[Any, Any]]
      val shape = source_config("shape").asInstanceOf[Vector[Int]].toList
      val result = (0.8, makeInputLayer(source._1 + "Input", shape, "SignalReader", MapGeneGroupDescriptor(
          "source" -> EnumGeneDescriptor(List(repository._1.toString)),
          "name" -> EnumGeneDescriptor(List(source._1.toString))
        )))
      result
    })
  }).toList ++ List(
    // Cycle
    ("HT_DCPERIOD", 1),
    ("HT_DCPHASE", 1),
    ("HT_PHASOR", 2),
    ("HT_SINE", 2),
    ("HT_TRENDMODE", 1),
    // Momentum
    ("ADX", 1),
    ("ADXR", 1),
    ("APO", 1),
    ("AROON", 2),
    ("AROONOSC", 1),
    ("BOP", 1),
    ("CCI", 1),
    ("CMO", 1),
    ("DX", 1),
    ("MACD", 3),
    ("MACDEXT", 3),
    ("MACDFIX", 3),
    ("MFI", 1),
    ("MINUS_DI", 1),
    ("MINUS_DM", 1),
    ("MOM", 1),
    ("PLUS_DI", 1),
    ("PLUS_DM", 1),
    ("PPO", 1),
    ("ROC", 1),
    ("ROCP", 1),
    ("ROCR", 1),
    ("ROCR100", 1),
    ("RSI", 1),
    ("STOCH", 2),
    ("STOCHF", 2),
    ("STOCHRSI", 2),
    ("TRIX", 1),
    ("ULTOSC", 1),
    ("WILLR", 1),
    // Overlap
    ("BBANDS", 3),
    ("DEMA", 1),
    ("EMA", 1),
    ("HT_TRENDLINE", 1),
    ("KAMA", 1),
    ("MA", 1),
    ("MAMA", 2),
    ("MAVP", 1),
    ("MIDPOINT", 1),
    ("MIDPRICE", 1),
    ("SAR", 1),
    ("SAREXT", 1),
    ("SMA", 1),
    ("T3", 1),
    ("TEMA", 1),
    ("TRIMA", 1),
    ("WMA", 1),
    // Patterns
    ("CDL2CROWS", 1),
    ("CDL3BLACKCROWS", 1),
    ("CDL3INSIDE", 1),
    ("CDL3LINESTRIKE", 1),
    ("CDL3OUTSIDE", 1),
    ("CDL3STARSINSOUTH", 1),
    ("CDL3WHITESOLDIERS", 1),
    ("CDLABANDONEDBABY", 1),
    ("CDLADVANCEBLOCK", 1),
    ("CDLBELTHOLD", 1),
    ("CDLBREAKAWAY", 1),
    ("CDLCLOSINGMARUBOZU", 1),
    ("CDLCONCEALBABYSWALL", 1),
    ("CDLCOUNTERATTACK", 1),
    ("CDLDARKCLOUDCOVER", 1),
    ("CDLDOJI", 1),
    ("CDLDOJISTAR", 1),
    ("CDLDRAGONFLYDOJI", 1),
    ("CDLENGULFING", 1),
    ("CDLEVENINGDOJISTAR", 1),
    ("CDLEVENINGSTAR", 1),
    ("CDLGAPSIDESIDEWHITE", 1),
    ("CDLGRAVESTONEDOJI", 1),
    ("CDLHAMMER", 1),
    ("CDLHANGINGMAN", 1),
    ("CDLHARAMI", 1),
    ("CDLHARAMICROSS", 1),
    ("CDLHIGHWAVE", 1),
    ("CDLHIKKAKE", 1),
    ("CDLHIKKAKEMOD", 1),
    ("CDLHOMINGPIGEON", 1),
    ("CDLIDENTICAL3CROWS", 1),
    ("CDLINNECK", 1),
    ("CDLINVERTEDHAMMER", 1),
    ("CDLKICKING", 1),
    ("CDLKICKINGBYLENGTH", 1),
    ("CDLLADDERBOTTOM", 1),
    ("CDLLONGLEGGEDDOJI", 1),
    ("CDLLONGLINE", 1),
    ("CDLMARUBOZU", 1),
    ("CDLMATCHINGLOW", 1),
    ("CDLMATHOLD", 1),
    ("CDLMORNINGDOJISTAR", 1),
    ("CDLMORNINGSTAR", 1),
    ("CDLONNECK", 1),
    ("CDLPIERCING", 1),
    ("CDLRICKSHAWMAN", 1),
    ("CDLRISEFALL3METHODS", 1),
    ("CDLSEPARATINGLINES", 1),
    ("CDLSHOOTINGSTAR", 1),
    ("CDLSHORTLINE", 1),
    ("CDLSPINNINGTOP", 1),
    ("CDLSTALLEDPATTERN", 1),
    ("CDLSTICKSANDWICH", 1),
    ("CDLTAKURI", 1),
    ("CDLTASUKIGAP", 1),
    ("CDLTHRUSTING", 1),
    ("CDLTRISTAR", 1),
    ("CDLUNIQUE3RIVER", 1),
    ("CDLUPSIDEGAP2CROWS", 1),
    ("CDLXSIDEGAP3METHODS", 1),
    // Price
    ("AVGPRICE", 1),
    ("MEDPRICE", 1),
    ("TYPPRICE", 1),
    ("WCLPRICE", 1),
    ("BETA", 1),
    ("CORREL", 1),
    ("LINEARREG", 1),
    ("LINEARREG_ANGLE", 1),
    ("LINEARREG_INTERCEPT", 1),
    ("LINEARREG_SLOPE", 1),
    ("STDDEV", 1),
    ("TSF", 1),
    ("VAR", 1),
    // Volatility
    ("ATR", 1),
    ("NATR", 1),
    ("TRANGE", 1),
    // Volume
    ("AD", 1),
    ("ADOSC", 1),
    ("OBV", 1)
  ).map(talibFunction => {
    (0.2, makeInputLayer(talibFunction._1 + "Input", List(talibFunction._2), "TalibSignalReader", MapGeneGroupDescriptor(
      "talib_function" -> EnumGeneDescriptor(List(talibFunction._1))
    )))
  })

  /// Common Layers

  // TODO: Reshape, Lambda, RepeatVector?

  val DenseLayer = makeLayer("Dense", 1, Seq(
    "units" -> UnitsDescriptor, // TODO: Extend
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
    "filters" -> LongGeneDescriptor(1, 250),
    "padding" -> EnumGeneDescriptor("valid", "same"), // "same" // whatever...
    // "strides" -> LongGeneDescriptor(0, 3),
    "use_bias" -> BooleanDescriptor,
    "activity_regularizer" -> RegularizerDescriptor
  ) ++
    makeInitializerRegularizerConstraint("kernel") ++
    makeInitializerRegularizerConstraint("bias")

  val Conv1DLayer = makeLayer("Conv1D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(1, 4),
    "kernel_size" -> tupleify(1, LongGeneDescriptor(1, 30))
  ))

  val Conv2DLayer = makeLayer("Conv2D", List("Conv2D", "Conv2DTranspose"), 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(1, 4),
    "data_format" -> ImageDataFormatDescriptor,
    "kernel_size" -> tupleify(2, LongGeneDescriptor(1, 30))
  ))

  val SeparableConv2DLayer = makeLayer("SeparableConv2D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(1, 4),
    "data_format" -> ImageDataFormatDescriptor,
    "kernel_size" -> tupleify(2, LongGeneDescriptor(1, 30))
  ) ++
    makeInitializerRegularizerConstraint("depthwise") ++
    makeInitializerRegularizerConstraint("pointwise"))

  val Conv3DLayer = makeLayer("Conv3D", 1, ConvBase ++ Seq(
    "dilation_rate" -> LongGeneDescriptor(1, 4),
    "data_format" -> ImageDataFormatDescriptor,
    "kernel_size" -> tupleify(3, LongGeneDescriptor(1, 30))
  ))

  val LocallyConnected1DLayer = makeLayer("LocallyConnected1D", 1, ConvBase ++ Seq(
    "kernel_size" -> tupleify(1, LongGeneDescriptor(1, 30))
  ))

  val LocallyConnected2DLayer = makeLayer("LocallyConnected2D", 1, ConvBase ++ Seq(
    "data_format" -> ImageDataFormatDescriptor,
    "kernel_size" -> tupleify(2, LongGeneDescriptor(1, 30))
  ))

  val CroppingLayers = Range.inclusive(1, 3).toList.map(dims => (0.3, makeLayer(s"Cropping${dims}D", 1, Seq(
      "cropping" -> tupleify(dims, GeneGroupDescriptor(LongGeneDescriptor(0, 16), LongGeneDescriptor(0, 16)))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  )))

  val UpSamplingLayers = Range.inclusive(1, 3).toList.map(dims => (0.3, makeLayer(s"UpSampling${dims}D", 1, Seq(
      "size" -> tupleify(dims, LongGeneDescriptor(1, 16))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  )))

  val ZeroPaddingLayers = Range.inclusive(1, 3).toList.map(dims => (0.3, makeLayer(s"ZeroPadding${dims}D", 1, Seq(
      "padding" -> tupleify(dims, GeneGroupDescriptor(LongGeneDescriptor(0, 16), LongGeneDescriptor(0, 16)))
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  )))


  /// Pooling Layers

  val PoolingLayers = Range.inclusive(1, 3).toList.map(dims => (0.3, makeLayer(s"Pooling${dims}D", List(s"MaxPooling${dims}D", s"AveragePooling${dims}D"), 1, Seq(
      "pool_size" -> tupleify(dims, LongGeneDescriptor(0, 16)),
      "padding" -> EnumGeneDescriptor("valid", "same")
    ) ++ (if (dims > 1) Seq("data_format" -> ImageDataFormatDescriptor) else Seq())
  )))

  val GlobalPooling1DLayer = makeLayer("GlobalPooling1D", List("GlobalMaxPooling1D", "GlobalAveragePooling1D"), 1, Seq())
  val GlobalPooling2DLayer = makeLayer("GlobalPooling2D", List("GlobalMaxPooling2D", "GlobalAveragePooling2D"), 1, Seq(
    "data_format" -> ImageDataFormatDescriptor
  ))

  /// RNN Layers

  val RNNBase = Seq(
    "go_backwards" -> BooleanDescriptor,
    "return_sequences" -> BooleanDescriptor,
    "stateful" -> BooleanDescriptor,
    "unroll" -> BooleanDescriptor,
    "activity_regularizer" -> RegularizerDescriptor
  ) ++
    makeInitializerRegularizerConstraint("kernel") ++
    makeInitializerRegularizerConstraint("recurrent") ++
    makeInitializerRegularizerConstraint("bias")

  val SimpleRNNLayer = makeLayer("SimpleRNN", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor
  ))

  val GRULayer = makeLayer("GRU", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "recurrent_activation" -> ActivationDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor,
    "reset_after" -> BooleanDescriptor
  ))

  val LSTMLayer = makeLayer("LSTM", 1, RNNBase ++ Seq(
    "units" -> UnitsDescriptor,
    "recurrent_activation" -> ActivationDescriptor,
    "dropout" -> DoubleGeneDescriptor(0, 1),
    "recurrent_dropout" -> DoubleGeneDescriptor(0, 1),
    "use_bias" -> BooleanDescriptor,
    "unit_forget_bias" -> BooleanDescriptor
  ))

  /// Merge Layers

  val MergeOpLayer = makeLayer("Merge", List("Add", "Subtract", "Multiply", "Average", "Maximum"), 2, Seq())

  val ConcatenateLayer = makeLayer("Concatenate", 2, Seq(
    "axis" -> LongGeneDescriptor(1, 2)
  ))

  val DotLayer = makeLayer("Dot", 2, Seq(
    "axes" -> LongGeneDescriptor(1, 2)
  ))

  val MergeLayers = List(
    (5.0, MergeOpLayer),
    (1.0, ConcatenateLayer),
    (1.0, DotLayer)
  )

  /// Activation Layers

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

  val ActivationLayers = List(
    (0.7, LeakyReLULayer),
    (0.7, ELULayer),
    (0.7, PReLULayer),
    (0.7, ThresholdedReLU)
  ) ++ ActivationDescriptor.values.map(x => (0.7, makeLayer("Activation_" + x, List("Activation"), 1, Seq(
    "activation" -> EnumGeneDescriptor(List(x))
  ))))

  /// Configs

  val AdamConfig = MapGeneGroupDescriptor("AdamConfig",
    "optimizer" -> MapGeneGroupDescriptor(
      "type" -> EnumGeneDescriptor(List("Adam")),
      "config" -> MapGeneGroupDescriptor("lr" -> DoubleGeneDescriptor(0, 2))
    ),
    "batch_size" -> EnumGeneDescriptor(List(1)),
    "loss" -> EnumGeneDescriptor(List("mean_squared_error")),
    "window_length" -> LongGeneDescriptor(1, 100)
  )

  /// Configs

  val DuplicateLayer = EnumGeneDescriptor(List(Map("special" -> "duplicate", "inputs" -> List(0))))
  val SwapLayer = EnumGeneDescriptor(List(Map("special" -> "swap", "inputs" -> List(0, 1))))

  /// Lists

  val Configs = List[GeneDescriptor](
    AdamConfig
  )

  val NonInputLayers = List( // weigth -> descriptor
    (1.0, DuplicateLayer),
    (1.0, SwapLayer),
    (8.0, DenseLayer),
    (1.0, DropoutLayer),
    (0.6, GaussianNoiseLayer),
    (0.8, FlattenLayer),
    (0.5, ActivityRegularizationLayer),
    (0.5, Conv1DLayer),
    (0.5, Conv2DLayer),
    (0.5, SeparableConv2DLayer),
    (0.5, Conv3DLayer),
    (0.5, LocallyConnected1DLayer),
    (0.5, LocallyConnected2DLayer),
    (0.5, GlobalPooling1DLayer),
    (0.5, GlobalPooling2DLayer),
    (1.0, SimpleRNNLayer),
    (1.0, GRULayer),
    (1.0, LSTMLayer)
  ) ++
    ActivationLayers ++
    CroppingLayers ++
    MergeLayers ++
    PoolingLayers ++
    UpSamplingLayers ++
    ZeroPaddingLayers ++
    List()

  val Layers = NonInputLayers ++ InputLayers
}
