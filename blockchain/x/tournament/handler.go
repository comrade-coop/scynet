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
	//call keeper's submitAgent
	//withdraw agent publish fee

	//agent module should hold a balance of all submitted fees that are related to its transactions.
	//For every submitAgent, withdraw that balance from agent module and include it in the upcoming tournament's reward
	//downside: fees submitted before the start of the tournament and after the last submitAgent for that tournament will not be included in that tournament, but the next one

	return sdk.Result{} // return
}
