package common.registry.exceptions

import java.lang.IllegalArgumentException

class TrainingJobExistsException(msg: String?) : IllegalArgumentException(msg) {
}