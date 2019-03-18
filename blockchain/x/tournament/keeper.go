package scynet

import (
	"strconv"

	"github.com/cosmos/cosmos-sdk/codec"
	"github.com/cosmos/cosmos-sdk/x/bank"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// Keeper maintains the link to data storage and exposes getter/setter methods for the various parts of the state machine
type Keeper struct {
	coinKeeper bank.Keeper // to collect various fees

	candidateAgentsStoreKey    sdk.StoreKey
	challengerDatasetsStoreKey sdk.StoreKey

	tournamentsStoreKey sdk.StoreKey //key is the UNIX timestamp of tournament start

	cdc *codec.Codec // The wire codec for binary encoding/decoding.
}

type Signal struct {
	ciphertext []byte
	key        []byte
	number     int
}

type CandidateAgent struct {
	signals      []Signal
	disqualified bool
}

type ChallengerDataset struct {
	inputs           []byte
	encryptedOutputs []byte
}

type UUID [16]byte

// domain config - signalsUrl = true,false; inputsUrl = true,false

type Tournament struct {
	Agents      []UUID // List of UUIDs of submitted agents
	Reward      sdk.Coins
	Challengers []sdk.AccAddress
}

type blockTime uint64

type tournamentNumber int

// helper functions
func blockTimeToSignalNumber(time blockTime) tournamentNumber {
	return 0
}

func blockTimeToTournamentNumber(time blockTime) tournamentNumber {
	return 0
}

func tournamentNumberToBytes(num tournamentNumber) []byte {
	return []byte(strconv.Itoa((int)(num)))
}

// NewKeeper creates new instances of the scynet Keeper
func NewKeeper(coinKeeper bank.Keeper, agentsStoreKey sdk.StoreKey, cdc *codec.Codec) Keeper {
	return Keeper{
		coinKeeper: coinKeeper,
		cdc:        cdc,
	}
}

func (k Keeper) saveTournament(ctx sdk.Context, num tournamentNumber, t Tournament) {
	key := tournamentNumberToBytes(num)

	store := ctx.KVStore(k.tournamentsStoreKey)
	store.Set(key, k.cdc.MustMarshalBinaryBare(t))
}

func (k Keeper) getOrCreateTournament(ctx sdk.Context, num tournamentNumber) Tournament {
	store := ctx.KVStore(k.tournamentsStoreKey)
	key := tournamentNumberToBytes(num)

	if store.Get(key) == nil {
		store.Set(key, k.cdc.MustMarshalBinaryBare(Tournament{
			Agents:      []UUID{}, //TODO: Better init
			Reward:      sdk.Coins{},
			Challengers: []sdk.AccAddress{},
		}))

	}

	var tournament Tournament
	k.cdc.MustUnmarshalBinaryBare(store.Get(key), &tournament)
	return tournament
}

func (k Keeper) submitFee(ctx sdk.Context, fee sdk.Coins, num tournamentNumber) {
	t := k.getOrCreateTournament(ctx, num)
	//check if tournament has not started yet (aka it has to be the latest one)

	t.Reward = t.Reward.Plus(fee) //TODO: Check if this is ok

	k.saveTournament(ctx, num, t)
}

func (k Keeper) submitAgent(uuid UUID, num tournamentNumber) {
	//calls agent to create a new agent object (with null performance for now)
	//ensures that UUID is unique

}
