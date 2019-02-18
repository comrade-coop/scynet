# ScyNet

This is the source code for [ScyNet](http://www.scynet.ai/), a decentralized network for creating and training autonomous AI agents.

## Running

### Linux / Mac

* Install .NET Core SDK (or Visual Studio on Windows)
* Run `scripts/run-local.sh` to start the controller on a single node.
  * It runs the `src/Scynet.LocalSilo` and `src/Scynet.HatcheryFacade` projects.
  * You can run them manually using `dotnet run …`

### Windows

* Install Visual Studio with the .NET Core SDK
* Open `ScynetLocalSilo.sln` with Visual Studio.
* Run the `Scynet.LocalSilo` project to start the controller.
* Run the `Scynet.HatcheryFacade` to start the facade of the hatchery. You might need to do that in a separate Visual Studio instance.
