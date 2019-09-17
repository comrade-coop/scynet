import { Tournament } from '../model/tournament';

export const LatestTournament: Tournament = {
        number: 5,
        submitted_agents: ["ABC", "agent1"],
        validated_agents: [
            {
                agent: "ABC",
                performance: 90,
                reward: 370
            },
            {
                agent: "AAA",
                performance: 70,
                reward: 130
            },
        ],
        reward_balance: 500
}