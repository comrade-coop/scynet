package scynet

import (
	"github.com/cosmos/cosmos-sdk/codec"
)

// RegisterCodec registers concrete types on the Amino codec
func RegisterCodec(cdc *codec.Codec) {
	cdc.RegisterConcrete(MsgPublishAgentPrice{}, "scynet/PublishAgentPrice", nil)
	cdc.RegisterConcrete(MsgPublishDataPrice{}, "scynet/PublishDataPrice", nil)
	cdc.RegisterConcrete(MsgRentAgent{}, "scynet/RentAgent", nil)
}
