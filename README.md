# Getting up and running

## Windows

To run the wrapper without any configuration, double-click the JAR file or run the following command in the terminal.
You may add the flag `--help` to see the available options.

```bash
java -jar sep-2.0.jar
```

You then can configure everything from the graphical user interface as described in the [wrapper section](#wrapper).

## Additional target platforms

OSX and Linux are only partially supported by the wrapper graphical user interface at this time.
You should instead use the wrapper cmd tools to start the server and clients.

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

<br />

You can always run the following command to see all the available options:

```bash
java -jar sep-1.0.jar --help
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
