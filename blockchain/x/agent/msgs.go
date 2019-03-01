package scynet

import (
	"encoding/json"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

const route = "agent"

// AgentStatus - the status of an agent
type AgentStatus int

const (
	// Unavailable - the agent never existed or is discontinued
	Unavailable AgentStatus = 0
	// Subscription - the agent can be subscribed to
	Subscription AgentStatus = 1
	// Sale - the agent can be bought
	Sale AgentStatus = 2
)

// MsgPublishAgentPrice - Transaction which changes an agent's price. If it never existed before, the agent is also registered
type MsgPublishAgentPrice struct {
	Owner  sdk.AccAddress
	UUID   [16]byte
	Status AgentStatus
	Price  sdk.Coins
}

// Route - implement Msg.Route for MsgPublishAgentPrice
func (msg MsgPublishAgentPrice) Route() string { return route }

// Type - implement Msg.Type for MsgPublishAgentPrice
func (msg MsgPublishAgentPrice) Type() string { return "publish_agent_price" }

// ValidateBasic - implement Msg.ValidateBasic for MsgPublishAgentPrice
func (msg MsgPublishAgentPrice) ValidateBasic() sdk.Error {
	if msg.Owner.Empty() {
		return sdk.ErrInvalidAddress(msg.Owner.String())
	}
	if msg.UUID == [16]byte{} {
		return sdk.ErrUnknownRequest("empty UUIDs are not allowed")
	}
	return nil
}

// GetSignBytes - implement Msg.GetSignBytes for MsgPublishAgentPrice
func (msg MsgPublishAgentPrice) GetSignBytes() []byte {
	b, err := json.Marshal(msg)
	if err != nil {
		panic(err)
	}
	return sdk.MustSortJSON(b)
}

// GetSigners - implement Msg.GetSigners for MsgPublishAgentPrice
func (msg MsgPublishAgentPrice) GetSigners() []sdk.AccAddress {
	return []sdk.AccAddress{msg.Owner}
}

//
// MsgPublishData - Transaction which creates a new data agent. It includes MsgPublishAgentPrice.
type MsgPublishData struct {
	MsgPublishAgentPrice
	Shapes [][]int
}

// Route - implement Msg.Route for MsgPublishData
func (msg MsgPublishData) Route() string { return route }

// Type - implement Msg.Type for MsgPublishData
func (msg MsgPublishData) Type() string { return "publish_data" }

// ValidateBasic - implement Msg.ValidateBasic for MsgPublishData
func (msg MsgPublishData) ValidateBasic() sdk.Error {
	if msg.Owner.Empty() {
		return sdk.ErrInvalidAddress(msg.Owner.String())
	}
	if msg.UUID == [16]byte{} {
		return sdk.ErrUnknownRequest("empty UUIDs are not allowed")
	}
	if msg.Shapes == nil {
		return sdk.ErrUnknownRequest("empty Shapes are not allowed")
	}
	for _, shape := range msg.Shapes {
		if shape == nil {
			return sdk.ErrUnknownRequest("empty Shapes are not allowed")
		}
	}
	return nil
}

// GetSignBytes - implement Msg.GetSignBytes for MsgPublishData
func (msg MsgPublishData) GetSignBytes() []byte {
	b, err := json.Marshal(msg)
	if err != nil {
		panic(err)
	}
	return sdk.MustSortJSON(b)
}

// GetSigners - implement Msg.GetSigners for MsgPublishData
func (msg MsgPublishData) GetSigners() []sdk.AccAddress {
	return []sdk.AccAddress{msg.Owner}
}

//
// MsgRentAgent - Transaction which signifies that an agent is rented
type MsgRentAgent struct {
	Sender   sdk.AccAddress
	UUID     [16]byte
	Quantity int
}

// Route - implement Msg.Route for MsgRentAgent
func (msg MsgRentAgent) Route() string { return route }

// Type - implement Msg.Type for MsgRentAgent
func (msg MsgRentAgent) Type() string { return "rent_agent" }

// ValidateBasic - implement Msg.ValidateBasic for MsgRentAgent
func (msg MsgRentAgent) ValidateBasic() sdk.Error {
	if msg.Sender.Empty() {
		return sdk.ErrInvalidAddress(msg.Sender.String())
	}
	if msg.UUID == [16]byte{} {
		return sdk.ErrUnknownRequest("empty UUIDs are not allowed")
	}
	if msg.Quantity == 0 {
		return sdk.ErrUnknownRequest("zero Quantity is not allowed")
	}
	return nil
}

// GetSignBytes - implement Msg.GetSignBytes for MsgRentAgent
func (msg MsgRentAgent) GetSignBytes() []byte {
	b, err := json.Marshal(msg)
	if err != nil {
		panic(err)
	}
	return sdk.MustSortJSON(b)
}

// GetSigners - implement Msg.GetSigners for MsgRentAgent
func (msg MsgRentAgent) GetSigners() []sdk.AccAddress {
	return []sdk.AccAddress{msg.Sender}
}
