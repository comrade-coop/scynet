package scynet

import (
	"github.com/cosmos/cosmos-sdk/codec"
	"github.com/cosmos/cosmos-sdk/x/bank"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// Keeper maintains the link to data storage and exposes getter/setter methods for the various parts of the state machine
type Keeper struct {
	coinKeeper bank.Keeper // to collect various fees

	agentsStoreKey sdk.StoreKey // Unexposed key to access agent store from sdk.Context
	//these are validated agents. For each one, we store its current price, status (for subscription, for sale, unavailable, etc..)
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

// GetAgent - returns the byte representation of the agent behind the given id
func (k Keeper) GetAgent(ctx sdk.Context, id string) string {
	store := ctx.KVStore(k.agentsStoreKey)
	bz := store.Get([]byte(id))
	return string(bz)
}

//TODO methods:

func (k Keeper) createAgent(ctx sdk.Context, id string, score int) {}

func (k Keeper) updateAgentScore(ctx sdk.Context, id string, score int) {}

type AgentStatus int

const (
	Subscription AgentStatus = 0
	Sale         AgentStatus = 1
	Unavailable  AgentStatus = 2
)

func (k Keeper) updateAgentStatus(ctx sdk.Context, id string, status AgentStatus, price int) {}

func (k Keeper) rentAgent() {}
