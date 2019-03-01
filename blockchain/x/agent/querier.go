package scynet

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
	uuid "github.com/google/uuid"
	abci "github.com/tendermint/tendermint/abci/types"
)

// query endpoints supported by the governance Querier
const (
	QueryGetAgent = "get_agent"
)

// NewQuerier is the module level router for state queries
func NewQuerier(keeper Keeper) sdk.Querier {
	return func(ctx sdk.Context, path []string, req abci.RequestQuery) (res []byte, err sdk.Error) {
		switch path[0] {
		case QueryGetAgent:
			return queryGetAgent(ctx, path[1:], req, keeper)
		default:
			return nil, sdk.ErrUnknownRequest("unknown agent query endpoint")
		}
	}
}

func queryGetAgent(ctx sdk.Context, path []string, req abci.RequestQuery, keeper Keeper) (res []byte, err sdk.Error) {

	id, error := uuid.Parse(path[0])
	if error != nil {
		return []byte{}, sdk.ErrUnknownRequest("wrong UUID formatting")
	}

	value := keeper.GetAgent(ctx, id)

	if value.Status == Unavailable {
		return []byte{}, sdk.ErrUnknownRequest("agent does not exist")
	}

	return []byte("TODO"), nil
}
