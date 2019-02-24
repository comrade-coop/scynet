package scynet

import (
	sdk "github.com/cosmos/cosmos-sdk/types"
)

type MsgPublishAgentPrice struct {
	Sender sdk.AccAddress
	UUID   string
	Status AgentStatus //TODO: This is defined in keeper.go. Bad practice
	Price  sdk.Coins
}

type MsgPublishDataPrice struct {
	Sender   sdk.AccAddress
	UUID     string
	Status   AgentStatus
	Price    sdk.Coins
	metadata int //TODO: Make valid data structure
}

type MsgRentAgent struct {
	Sender   sdk.AccAddress
	UUID     string
	Quantity int
}

// TODO: Implement the expected message interface for the rest of the messages
