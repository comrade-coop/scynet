# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: Hatchery.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


import Shared_pb2 as Shared__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='Hatchery.proto',
  package='Scynet',
  syntax='proto3',
  serialized_options=_b('\252\002\006Scynet'),
  serialized_pb=_b('\n\x0eHatchery.proto\x12\x06Scynet\x1a\x0cShared.proto\"M\n\x18\x43omponentRegisterRequest\x12\x0c\n\x04uuid\x18\x02 \x01(\t\x12\x0f\n\x07\x61\x64\x64ress\x18\x01 \x01(\t\x12\x12\n\nrunnerType\x18\x03 \x03(\t\"\x1b\n\x19\x43omponentRegisterResponse\"4\n\x14\x41gentRegisterRequest\x12\x1c\n\x05\x61gent\x18\x02 \x01(\x0b\x32\r.Scynet.Agent\"\x17\n\x15\x41gentRegisterResponse\"*\n\x1a\x43omponentUnregisterRequest\x12\x0c\n\x04uuid\x18\x01 \x01(\t\"O\n\x11\x41gentStoppedEvent\x12\x1c\n\x05\x61gent\x18\x01 \x01(\x0b\x32\r.Scynet.Agent\x12\x0e\n\x06resaon\x18\x02 \x01(\t\x12\x0c\n\x04\x63ode\x18\x03 \x01(\x04\x32\xbc\x02\n\x08Hatchery\x12Z\n\x11RegisterComponent\x12 .Scynet.ComponentRegisterRequest\x1a!.Scynet.ComponentRegisterResponse\"\x00\x12N\n\rRegisterAgent\x12\x1c.Scynet.AgentRegisterRequest\x1a\x1d.Scynet.AgentRegisterResponse\"\x00\x12I\n\x13UnregisterComponent\x12\".Scynet.ComponentUnregisterRequest\x1a\x0c.Scynet.Void\"\x00\x12\x39\n\x0c\x41gentStopped\x12\x19.Scynet.AgentStoppedEvent\x1a\x0c.Scynet.Void\"\x00\x42\t\xaa\x02\x06Scynetb\x06proto3')
  ,
  dependencies=[Shared__pb2.DESCRIPTOR,])




_COMPONENTREGISTERREQUEST = _descriptor.Descriptor(
  name='ComponentRegisterRequest',
  full_name='Scynet.ComponentRegisterRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uuid', full_name='Scynet.ComponentRegisterRequest.uuid', index=0,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='address', full_name='Scynet.ComponentRegisterRequest.address', index=1,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='runnerType', full_name='Scynet.ComponentRegisterRequest.runnerType', index=2,
      number=3, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=40,
  serialized_end=117,
)


_COMPONENTREGISTERRESPONSE = _descriptor.Descriptor(
  name='ComponentRegisterResponse',
  full_name='Scynet.ComponentRegisterResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=119,
  serialized_end=146,
)


_AGENTREGISTERREQUEST = _descriptor.Descriptor(
  name='AgentRegisterRequest',
  full_name='Scynet.AgentRegisterRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='agent', full_name='Scynet.AgentRegisterRequest.agent', index=0,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=148,
  serialized_end=200,
)


_AGENTREGISTERRESPONSE = _descriptor.Descriptor(
  name='AgentRegisterResponse',
  full_name='Scynet.AgentRegisterResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=202,
  serialized_end=225,
)


_COMPONENTUNREGISTERREQUEST = _descriptor.Descriptor(
  name='ComponentUnregisterRequest',
  full_name='Scynet.ComponentUnregisterRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uuid', full_name='Scynet.ComponentUnregisterRequest.uuid', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=227,
  serialized_end=269,
)


_AGENTSTOPPEDEVENT = _descriptor.Descriptor(
  name='AgentStoppedEvent',
  full_name='Scynet.AgentStoppedEvent',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='agent', full_name='Scynet.AgentStoppedEvent.agent', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='resaon', full_name='Scynet.AgentStoppedEvent.resaon', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='code', full_name='Scynet.AgentStoppedEvent.code', index=2,
      number=3, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=271,
  serialized_end=350,
)

_AGENTREGISTERREQUEST.fields_by_name['agent'].message_type = Shared__pb2._AGENT
_AGENTSTOPPEDEVENT.fields_by_name['agent'].message_type = Shared__pb2._AGENT
DESCRIPTOR.message_types_by_name['ComponentRegisterRequest'] = _COMPONENTREGISTERREQUEST
DESCRIPTOR.message_types_by_name['ComponentRegisterResponse'] = _COMPONENTREGISTERRESPONSE
DESCRIPTOR.message_types_by_name['AgentRegisterRequest'] = _AGENTREGISTERREQUEST
DESCRIPTOR.message_types_by_name['AgentRegisterResponse'] = _AGENTREGISTERRESPONSE
DESCRIPTOR.message_types_by_name['ComponentUnregisterRequest'] = _COMPONENTUNREGISTERREQUEST
DESCRIPTOR.message_types_by_name['AgentStoppedEvent'] = _AGENTSTOPPEDEVENT
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ComponentRegisterRequest = _reflection.GeneratedProtocolMessageType('ComponentRegisterRequest', (_message.Message,), dict(
  DESCRIPTOR = _COMPONENTREGISTERREQUEST,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.ComponentRegisterRequest)
  ))
_sym_db.RegisterMessage(ComponentRegisterRequest)

ComponentRegisterResponse = _reflection.GeneratedProtocolMessageType('ComponentRegisterResponse', (_message.Message,), dict(
  DESCRIPTOR = _COMPONENTREGISTERRESPONSE,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.ComponentRegisterResponse)
  ))
_sym_db.RegisterMessage(ComponentRegisterResponse)

AgentRegisterRequest = _reflection.GeneratedProtocolMessageType('AgentRegisterRequest', (_message.Message,), dict(
  DESCRIPTOR = _AGENTREGISTERREQUEST,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.AgentRegisterRequest)
  ))
_sym_db.RegisterMessage(AgentRegisterRequest)

AgentRegisterResponse = _reflection.GeneratedProtocolMessageType('AgentRegisterResponse', (_message.Message,), dict(
  DESCRIPTOR = _AGENTREGISTERRESPONSE,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.AgentRegisterResponse)
  ))
_sym_db.RegisterMessage(AgentRegisterResponse)

ComponentUnregisterRequest = _reflection.GeneratedProtocolMessageType('ComponentUnregisterRequest', (_message.Message,), dict(
  DESCRIPTOR = _COMPONENTUNREGISTERREQUEST,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.ComponentUnregisterRequest)
  ))
_sym_db.RegisterMessage(ComponentUnregisterRequest)

AgentStoppedEvent = _reflection.GeneratedProtocolMessageType('AgentStoppedEvent', (_message.Message,), dict(
  DESCRIPTOR = _AGENTSTOPPEDEVENT,
  __module__ = 'Hatchery_pb2'
  # @@protoc_insertion_point(class_scope:Scynet.AgentStoppedEvent)
  ))
_sym_db.RegisterMessage(AgentStoppedEvent)


DESCRIPTOR._options = None

_HATCHERY = _descriptor.ServiceDescriptor(
  name='Hatchery',
  full_name='Scynet.Hatchery',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  serialized_start=353,
  serialized_end=669,
  methods=[
  _descriptor.MethodDescriptor(
    name='RegisterComponent',
    full_name='Scynet.Hatchery.RegisterComponent',
    index=0,
    containing_service=None,
    input_type=_COMPONENTREGISTERREQUEST,
    output_type=_COMPONENTREGISTERRESPONSE,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='RegisterAgent',
    full_name='Scynet.Hatchery.RegisterAgent',
    index=1,
    containing_service=None,
    input_type=_AGENTREGISTERREQUEST,
    output_type=_AGENTREGISTERRESPONSE,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='UnregisterComponent',
    full_name='Scynet.Hatchery.UnregisterComponent',
    index=2,
    containing_service=None,
    input_type=_COMPONENTUNREGISTERREQUEST,
    output_type=Shared__pb2._VOID,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='AgentStopped',
    full_name='Scynet.Hatchery.AgentStopped',
    index=3,
    containing_service=None,
    input_type=_AGENTSTOPPEDEVENT,
    output_type=Shared__pb2._VOID,
    serialized_options=None,
  ),
])
_sym_db.RegisterServiceDescriptor(_HATCHERY)

DESCRIPTOR.services_by_name['Hatchery'] = _HATCHERY

# @@protoc_insertion_point(module_scope)