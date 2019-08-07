package ai.scynet.protocol.exceptions

import java.lang.IllegalArgumentException

class JobNotAvailableException(msg: String?): IllegalArgumentException(msg) {
}