import { Agent } from './agent';

export class NetworkState {
    address: string;
    voting_power: number;
    validated_agents: Agent[];
    published_data: Agent[];
}
