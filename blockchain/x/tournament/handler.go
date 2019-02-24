package scynet

import (
	"fmt"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// NewHandler returns a handler for "agent" type messages.
func NewHandler(keeper Keeper) sdk.Handler {
	return func(ctx sdk.Context, msg sdk.Msg) sdk.Result {
		switch msg := msg.(type) {
		case MsgSubmitAgent:
			return handleMsgSubmitAgent(ctx, keeper, msg)
		// TODO: Handle the rest
		default:
			errMsg := fmt.Sprintf("Unrecognized agent Msg type: %v", msg.Type())
			return sdk.ErrUnknownRequest(errMsg).Result()
		}
	}
}

// Handle handleMsgSubmitAgent
func handleMsgSubmitAgent(ctx sdk.Context, keeper Keeper, msg MsgSubmitAgent) sdk.Result {
	//TODO: Perform stateful validity checks

	//TODO: Update state
	return sdk.Result{} // return
}
