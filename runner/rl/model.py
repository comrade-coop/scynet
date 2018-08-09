from ..trainer import Model
from ..parser import build_model
from .environment import RLEnvironment
from keras import Model as KerasModel
from keras.layers import Dense, Reshape, Concatenate
from keras.utils import plot_model
from .rl_patches import TrainEpisodeLogger, DQNAgent
from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.processors import MultiInputProcessor


class RLModel(Model):
    # Steps
    max_iterations = 200
    iteration_learning_episodes = 2

    # Policy (exploration versus exploitation of actions)
    final_tau = 0.7

    # Early-stopping
    patience = 1

    def __init__(self, descriptor):
        (internal_model, inputs) = build_model(descriptor)
        self.needed_signal_descriptors = inputs

        actions_count = len(RLEnvironment.action_space)

        reshaped_outputs = [Reshape((-1,))(output) for output in internal_model.outputs]
        concatenated_output = Concatenate()(reshaped_outputs) if len(reshaped_outputs) > 1 else reshaped_outputs[0]
        dense_transform = Dense(actions_count)(concatenated_output)

        outer_model = KerasModel(inputs=internal_model.inputs, outputs=dense_transform)

        self.agent = DQNAgent(
            model=outer_model,
            nb_actions=actions_count,
            memory=SequentialMemory(limit=4000, window_length=descriptor['window_length']),
            nb_steps_warmup=100,
            target_model_update=0.01,
            policy=BoltzmannQPolicy(),
            processor=MultiInputProcessor(nb_inputs=len(inputs)) if len(inputs) > 1 else None,
        )

        self.agent.compile(optimizer=internal_model.optimizer, metrics=['mae'])
        self.env = None

    def plot(self, file_basename):
        plot_model(self.agent.model, file_basename + '.png')

    def train(self, trainer):
        self.env = RLEnvironment(trainer)
        learning_steps = trainer.get_episode_row_count('learning')

        # IDEA: Save weights from X iterations ago, and revert to them after early-stopping ends
        # The rationale is that we have to find the exact start of the overfit, otherwise validation/test perf will suffer

        iterations = 0
        last_score = 0.0
        stopping_iterations = 0
        policy_tau_change = self.final_tau ** (1 / self.max_iterations)

        for i in range(self.max_iterations):
            iterations += 1
            self.agent.policy.tau *= policy_tau_change

            self.env.set_episode('learning')
            self.agent.fit(
                self.env,
                nb_steps=learning_steps * self.iteration_learning_episodes,
                action_repetition=1,
                visualize=False,
                verbose=0,
                callbacks=[TrainEpisodeLogger()]
            )
            # training_score = env.last_result

            self.env.set_episode('validation')
            self.agent.test(self.env, nb_episodes=1, action_repetition=1, visualize=False)
            current_score = self.env.last_result

            if current_score < last_score:
                stopping_iterations += 1
                if stopping_iterations > self.patience:
                    break

            last_score = current_score

        return iterations

    def test(self, training_session):
        self.env = RLEnvironment(training_session.trainer)
        self.env.set_episode(training_session)
        self.agent.test(self.env, nb_episodes=1, action_repetition=1, visualize=False)

    def save_weights(self, to_file):
        self.agent.save_weights(to_file, overwrite=True)

    def load_weights(self, from_file):
        self.agent.load_weights(from_file)
