package scynet

import (
	"encoding/json"

	sdk "github.com/cosmos/cosmos-sdk/types"
)

// MsgSubmitAgent holds the message contents and implements Cosmos's expected message interface
type MsgSubmitAgent struct {
	Sender sdk.AccAddress
	UUID   string //TODO: Find a better type for this
}

// NewMsgSubmitAgent is a constructor function for MsgSubmitAgent
func NewMsgSubmitAgent(sender sdk.AccAddress, UUID string) MsgSubmitAgent {
	return MsgSubmitAgent{
		Sender: sender,
		UUID:   UUID,
	}
}

// Route module
func (msg MsgSubmitAgent) Route() string { return "agent" }

// Type of message in the module
func (msg MsgSubmitAgent) Type() string { return "submit_agent" }

// ValidateBasic Implements Msg.
func (msg MsgSubmitAgent) ValidateBasic() sdk.Error {
	if msg.Sender.Empty() {
		return sdk.ErrInvalidAddress(msg.Sender.String())
	}
	//TODO: Check for a valid UUID format
	return nil
}

// GetSignBytes Implements Msg.
func (msg MsgSubmitAgent) GetSignBytes() []byte {
	b, err := json.Marshal(msg)
	if err != nil {
		panic(err)
	}

	return sdk.MustSortJSON(b)
}

// GetSigners Implements Msg.
func (msg MsgSubmitAgent) GetSigners() []sdk.AccAddress {
	return []sdk.AccAddress{msg.Sender}
}

// TODO: Implement the expected message interface for the rest of the messages
