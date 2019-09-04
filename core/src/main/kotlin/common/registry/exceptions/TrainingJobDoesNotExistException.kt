package common.registry.exceptions

import java.lang.IllegalArgumentException

class TrainingJobDoesNotExistException(msg: String?): IllegalArgumentException(msg) {
}