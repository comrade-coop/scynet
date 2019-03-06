package agent

import (
	"fmt"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// NewHandler returns a handler for "agent" type messages.
func NewHandler(keeper Keeper) sdk.Handler {
	return func(ctx sdk.Context, msg sdk.Msg) sdk.Result {
		switch msg := msg.(type) {
		case MsgPublishAgentPrice:
			return handleMsgPublishAgentPrice(ctx, keeper, msg)
		case MsgPublishData:
			return handleMsgPublishData(ctx, keeper, msg)
		case MsgRentAgent:
			return handleMsgRentAgent(ctx, keeper, msg)
		default:
			errMsg := fmt.Sprintf("Unrecognized agent Msg type: %v", msg.Type())
			return sdk.ErrUnknownRequest(errMsg).Result()
		}
	}
}

func handleMsgPublishAgentPrice(ctx sdk.Context, keeper Keeper, msg MsgPublishAgentPrice) sdk.Result {
	if !keeper.CanOwn(ctx, msg.UUID, msg.Owner) {
		return sdk.ErrUnauthorized("Incorrect Owner").Result()
	}
	keeper.CreateAgentIfNonexistent(ctx, msg.UUID, msg.Owner, nil)
	keeper.UpdateAgentPrice(ctx, msg.UUID, msg.Status, msg.Price)
	return sdk.Result{}
}

func handleMsgPublishData(ctx sdk.Context, keeper Keeper, msg MsgPublishData) sdk.Result {
	if !keeper.CanOwn(ctx, msg.UUID, msg.Owner) {
		return sdk.ErrUnauthorized("Incorrect Owner").Result()
	}
	keeper.CreateAgentIfNonexistent(ctx, msg.UUID, msg.Owner, msg.Shapes)
	keeper.UpdateAgentPrice(ctx, msg.UUID, msg.Status, msg.Price)
	return sdk.Result{}
}

func handleMsgRentAgent(ctx sdk.Context, keeper Keeper, msg MsgRentAgent) sdk.Result {
	return sdk.Result{} // TODO: Implement renting
}
