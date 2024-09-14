# About

This is a project developed in a practical course at the Ludwig-Maximilians-Universität München over the course of a semester.

The goal was to develop a networked game that can be played by multiple clients and / or bots. 
The requirements to be implemented may be found [here](http://media.wizards.com/2017/rules/roborally_rules.pdf). 

# Getting up and running

## Windows

To run the wrapper without any configuration, double-click the JAR file or run the following command in the terminal.
You may add the flag `--help` to see the available options.

```bash
java -jar sep-2.0.jar
```

You then can configure everything from the graphical user interface as described in the [wrapper](#wrapper) section.

## OSX

To run the wrapper without any configuration, run the following command in the terminal.
You may add the flag `--help` to see the available options.

Note that JavaFX 19.X is only partially supported on the currently newest version of OSX (Sonoma 14.X).
As such, the wrapper GUI may not work as expected.
For example, the GUI will always experience a timeout of five seconds when opening
(described in [this](https://bugs.openjdk.org/browse/JDK-8315657) issue).

```bash
java -jar sep-2.0-aarch64.jar
```

You then can configure everything from the graphical user interface as described in the [wrapper](#wrapper) section.
You will only have to use the `aarch` version of the JAR file if you are running the Wrapper GUI or the 'human' client.
For the server or an agent, the normal JAR file will also work.

OSX may run into various issues when running the wrapper GUI
if something does not work as expected use the [CLI](#cli) instead.

## Additional target platforms

The Wrapper GUI is not supported on any other platform. You will have to use the [CLI](#cli) instead.

Note to run a 'human' client,
you will need to have JavaFX 19.X installed for your system and add it to the module path as follows:

```bash
java --module-path "[...]\javafx-sdk-19\lib" --add-modules javafx.swing,javafx.controls,javafx.fxml -jar sep-2.0.jar --cl [additional flags]
```

## CLI

Note if you are on a OSX machine, you will have to use the `aarch` version of the JAR file if you are starting a GUI.

To start a server in default configuration:

```bash
java -jar sep-2.0.jar --nocmd --sv
```

The server is now started at your localhost on port `8080`.
To change the port, add the `--port` flag as follows `--port 7000`.

<br />

To start a 'human' client in default configuration:

```bash
java -jar sep-2.0.jar --cl
```

If you want to change to which server the 'human' client connects to,
change in the main menu the server address and port in the top right corner.

<br />

To start an 'agent' client in default configuration:

```bash
java -jar sep-2.0.jar --cl --isAgent --name <agent-name>
```

The agent is now started and will try its best to connect to the server at `localhost:8080`.

Note that you need to specify a name for any agent you start.

To change to which server the 'agent' client connects,
add the `--addr <server-address>` flag and / or the `--port <server-port>` flag.
To change the default Session ID (Group ID) (default is `geselliges-getreide`)
of the agent add the `--sid <session-id>` flag.
A full example would be:

```bash
java -jar sep-2.0.jar --cl --isAgent --name "My Agent" --addr localhost --port 7000 --sid my-special-id
```

## Additional flags (mainly used for development and debugging):

### Wrapper Help

Valid wrapper program arguments in descending order of precedence.
All arguments that are not consumed by the wrapper will be passed down to the follow-up process.
Invalid arguments will be ignored (the follow-up process might not ignore invalid arguments and may fail to start).

Usage:

```bash
java -jar sep-2.0.jar [--cmd] [--sv] [--cl] [--nocmd] [--noclose] [--help]
```

```
  --cmd         Start a new process terminal and run this application in it.
  --sv          Will instantly start a server process.
  --cl          Will instantly start a client process (IO is inherited to calling process).
  --nocmd       Will not create a new process terminal for the follow-up server process.
  --noclose     If allowed a new process terminal will not be closed after the follow-up process has exited.
  --help        Print wrapper help message.
```

### Client Help

Valid view program arguments in descending order of precedence.

Usage:

```bash
java -cp sep-2.0.jar sep.view.Launcher [--dev] [--isAgent] [--addr ADDR] [--port PORT] [--sid SID] [--name NAME] [--difficulty DIFFICULTY] [--allowLegacyAgents] [--help]
```

```
  --dev                         Start mock game view (if also started with the [--isAgent] flag, the agent mock view will be called instead).
  --isAgent                     Start agent view.
  --addr <ADDR>                 The address to auto connect to (if [--isAgent] flag is set). Default is localhost.
  --port <PORT>                 The port number to auto connect to (if [--isAgent] flag is set). Default is 8080.
  --sid <SID>                   The session ID to auto connect to (if [--isAgent] flag is set). Default is geselliges-getreide.
  --name <NAME>                 The name of the agent (if [--isAgent] flag is set).
  --difficulty <DIFFICULTY>     The difficulty of the agent (if [--isAgent] flag is set) (0 = Random, 1 = Q-Learning). Default is 1.
  --allowLegacyAgents           Allow legacy agent logic to be displayed in the client Graphical User Interface (the deprecated server agent logic will be used).
  --help                        Print view help message.
```

### Server Help

Valid server program arguments in descending order of precedence.

Usage:

```bash
java -cp sep-2.0.jar sep.server.Launcher [--port PORT] [--minRemotePlayers MIN_REMOTE_PLAYERS] [--minHumanPlayers MIN_HUMAN_PLAYERS] [--help]
```

```
  --port <PORT>                             The port number to listen on. Default is 8080.
  --minRemotePlayers <MIN_REMOTE_PLAYERS>   The minimum number of remote clients required to start a game. Default is 1. Only used in legacy games.
  --minHumanPlayers <MIN_HUMAN_PLAYERS>     The minimum number of human players required to start a game. Default is 1.
  --help                                    Print server help message.
```

# Wrapper

Under the menu option `CUSTOM START`,
you may configure the agents that will be started as well as start the server and a client at the same time.
You can add up to six agents to be started by the wrapper.

# Main Menu

Select `HOST SESSION` to connect to the specified server and host a session.
The session hosted will default to the Session ID (Group ID in the protocol) `geselliges-getreide`.
If a session with the same ID already exists on the server, the client will join that session instead.

To change the Session ID, type in the desired ID in the text field in the top right corner and select `JOIN SESSION`.

# Chatting

Commands are prefixed with a `/` and are case-sensitive. `Enter` to send a message.

```
/w "<player-name>" <message>    # Whisper to a player.
/h                              # Show all available commands.
/hide                           # Hide server infos (in game scene only).
/show                           # Show server infos (in game scene only).
```

# Documentation

The java-docs can be found [here](docs/index.html) (only available inside the private repo on gitlab).
