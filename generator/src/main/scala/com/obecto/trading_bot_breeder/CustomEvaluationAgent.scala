package com.obecto.trading_bot_breeder

import com.obecto.gattakka.EvaluationAgent


class CustomEvaluationAgent extends EvaluationAgent {

 override def onSignalReceived(data: Any): Unit = data match {
    case tradeResult : Double =>
      fitness = tradeResult
      println(fitness)
  }
}
