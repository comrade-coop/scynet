import { NetworkState } from "../model/network-state";


export const NetworkStateMock: NetworkState[] = [
    {
		address: "ABC",
		voting_power: 5,
		validated_agents: [
			{
				name: "agent2",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		],
		published_data: [
			{
                name: "agent2",
				last_tournament: 5,
				last_performance: 70,
				price: 0
            },
            {
                name: "agent6",
				last_tournament: 5,
				last_performance: 70,
				price: 0
            },
            {
                name: "agent7",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		]
    },
    {
		address: "AAA",
		voting_power: 5,
		validated_agents: [
			{
				name: "agent1",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		],
		published_data: [
			{
                name: "agent1",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		]
    },
    {
		address: "BBB",
		voting_power: 5,
		validated_agents: [
			{
				name: "agent3",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		],
		published_data: [
			{
                name: "agent3",
				last_tournament: 5,
				last_performance: 70,
				price: 0
			}
		]
	}
]
