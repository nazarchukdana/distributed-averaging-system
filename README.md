# Distributed Averaging System (DAS)

## Overview
The **Distributed Averaging System (DAS)** is a UDP-based application designed to operate in two modes: **Master** and **Slave**. The system collects numbers from multiple Slave processes, computes their average, and broadcasts the result to the network.

## Features
- **Master Mode**:
  - Collects numbers from Slaves.
  - Computes and broadcasts the average when triggered.
  - Terminates upon receiving a shutdown signal.
- **Slave Mode**:
  - Sends a number to the Master and terminates.
- **UDP Communication**: Uses UDP packets for number transmission and broadcasting.
- **Dynamic Role Assignment**: Automatically determines mode based on port availability.

## How It Works
1. **Master Mode** (if the UDP port is available):
   - Stores its initial number.
   - Listens for incoming numbers from Slaves.
   - Processes numbers as follows:
     - **Integer (not 0 or -1)**: Stores it for future calculations.
     - **0**: Computes and broadcasts the average.
     - **-1**: Broadcasts termination signal and exits.
2. **Slave Mode** (if the UDP port is in use):
   - Sends its number to the Master.
   - Terminates after successful transmission.

## Protocol
DAS follows a simple UDP-based protocol:
- **Integer values**: Sent by Slaves to the Master for averaging.
- **0**: Instructs the Master to compute and broadcast the average.
- **-1**: Triggers system termination.
- **Broadcasting**: The Master broadcasts computed averages and termination signals to all machines in the local network.

## Installation & Setup
1. **Clone the repository:**
   ```sh
   git clone <repository_url>
   cd <repository_directory>
   ```
2. **Compile the project:**
   ```sh
   javac DAS.java
   ```
3. **Run as Master:**
   ```sh
   java DAS <port> <initial_number>
   ```
4. **Run as Slave:**
   ```sh
   java DAS <port> <number>
   ```

## Methods Description
### `main()`
- Determines whether to run in Master or Slave mode based on UDP port availability.

### `runMaster(DatagramSocket socket, int masterNumber)`
- Handles Master mode operations:
  - Retrieves the broadcast address.
  - Listens for incoming numbers.
  - Computes and broadcasts the average when receiving `0`.
  - Terminates when receiving `-1`.

### `runSlave(int port, int number)`
- Handles Slave mode operations:
  - Sends its number to the Master.
  - Logs the transmission and terminates.

### `broadcastAverage(DatagramSocket socket, List<Integer> nums)`
- Computes and broadcasts the average of received numbers.

### `broadcastMessage(DatagramSocket socket, String message)`
- Sends a UDP broadcast message to all devices on the local network.

### `sendSignalAndTerminate(DatagramSocket socket)`
- Broadcasts a termination signal (-1) and stops the Master process.

### `calculateAverage(List<Integer> nums)`
- Computes the integer average of received numbers.

### `getBroadcastAddress()`
- Determines the network broadcast address for message transmission.

## Known Issues
- **No Timeout Handling:** The Master runs indefinitely if no `-1` signal is received.
- **Broadcast Address Retrieval:** Sometimes there is an issue that the program cannot find the broadcast address.

## Contributors
- **Author:** Dana Nazarchuk
- Created as a school project for Polish Japanese Academy of Information Technologies

