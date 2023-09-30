# DAP Protocol

**Simple**, **fast** and **reliable** transmission protocol for unicast and broadcast communication under the water for scuba divers! 

## Start Node

Run the `main` method of `ui/View` either from terminal or from your favourite IDE.

## Protocol Setup

To tweak timeout times, reachability ttl and more go to `protocol/ProtocolSetup`.

## Project structure

- `logger/` Simple logging mechanism (better than classic Java Logger)
- `protocol/` Business logic of the DAP protocol
  - `models/` Data structures package
  - `reachability/` Classes used for reachability information
  - `*` All other useful classes responsible for handling DAP
- `ui/` Neat Console UI, creates Protocol and connects as a Listener


