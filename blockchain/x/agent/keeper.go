package agent

import (
	"github.com/cosmos/cosmos-sdk/codec"
	"github.com/cosmos/cosmos-sdk/x/bank"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// Agent - data about an agent
type Agent struct {
	Owner       sdk.AccAddress
	Status      AgentStatus
	Shapes      [][]int
	Price       sdk.Coins
	LastScore   int
	LastScoreAt int
}

// Keeper maintains the link to data storage and exposes getter/setter methods for the various parts of the state machine
type Keeper struct {
	coinKeeper bank.Keeper // to collect various fees

	agentsStoreKey sdk.StoreKey // References a store of id => agent.
	//its most resent score in a tournament (if the agent is not data)
	//and a Queue of everyone that ever paid for that agent

	cdc *codec.Codec // The wire codec for binary encoding/decoding.
}

// NewKeeper creates new instances of the scynet Keeper
func NewKeeper(coinKeeper bank.Keeper, agentsStoreKey sdk.StoreKey, cdc *codec.Codec) Keeper {
	return Keeper{
		coinKeeper:     coinKeeper,
		agentsStoreKey: agentsStoreKey,
		cdc:            cdc,
	}
}

// GetAgent - returns the full representation of the agent behind the given id
func (k Keeper) GetAgent(ctx sdk.Context, id [16]byte) Agent {
	store := ctx.KVStore(k.agentsStoreKey)
	var agent Agent
	k.cdc.MustUnmarshalBinaryBare(store.Get(id[:]), &agent)
	return agent
}

// CanOwn - returns if an account can modify an agent (either because it does not exist or because the owner matches)
func (k Keeper) CanOwn(ctx sdk.Context, id [16]byte, owner sdk.AccAddress) bool {
	store := ctx.KVStore(k.agentsStoreKey)
	bz := store.Get(id[:])
	var agent Agent
	k.cdc.MustUnmarshalBinaryBare(bz, &agent)
	return bz == nil || agent.Owner.Equals(owner)
}

// CreateAgentIfNonexistent - register a new data agent.
func (k Keeper) CreateAgentIfNonexistent(ctx sdk.Context, id [16]byte, owner sdk.AccAddress, shapes [][]int) {
	store := ctx.KVStore(k.agentsStoreKey)
	if store.Get(id[:]) != nil {
		return
	}
	store.Set(id[:], k.cdc.MustMarshalBinaryBare(Agent{
		Owner:  owner,
		Shapes: shapes,
	}))
}

// UpdateAgentPrice - update an agent's price and status.
func (k Keeper) UpdateAgentPrice(ctx sdk.Context, id [16]byte, status AgentStatus, price sdk.Coins) {
	store := ctx.KVStore(k.agentsStoreKey)
	var agent Agent
	k.cdc.MustUnmarshalBinaryBare(store.Get(id[:]), &agent)
	agent.Status = status
	agent.Price = price
	store.Set(id[:], k.cdc.MustMarshalBinaryBare(agent))
}

// UpdateAgentScore - update an agent's score.
func (k Keeper) UpdateAgentScore(ctx sdk.Context, id [16]byte, score int, tournamentID int) {
	store := ctx.KVStore(k.agentsStoreKey)
	var agent Agent
	k.cdc.MustUnmarshalBinaryBare(store.Get(id[:]), &agent)
	agent.LastScore = score
	agent.LastScoreAt = tournamentID
	store.Set(id[:], k.cdc.MustMarshalBinaryBare(agent))
}

func (k Keeper) rentAgent() {}
