# Client -> Server

## List of Commands

`register <username> <password>`

`login <username> <password>`

`search <tag>`

`upload <file name> <title> <artist> <year> <file path> <tag>...`

`download <song ID>`

`help`

## List of Messages

### Data

`data <song ID> <offset> <bytes in Base64>`

### Notifications

`notify <song ID>`

# Server -> Client

`DATA: <song ID> <offset> <bytes in Base64>`

`REQUEST: <song ID> <file path>`

`REPLY: <message>`

`ERROR: <message>`

`<title> <line>;...`
