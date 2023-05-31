# ChatHub

ChatHub é um sistema de chat simples em Java. Ele usa sockets para comunicar entre cliente e servidor, e permite aos usuários enviar mensagens em salas de chat diferentes.

## Funcionalidades

- Conexão cliente-servidor utilizando sockets
- Criação e participação em diferentes salas de chat
- Envio e recebimento de mensagens em tempo real

## Como rodar

1. Compile o arquivo `ChatHub.java`
2. Rode o arquivo `ChatHub.java` com a porta desejada. Exemplo: `java Server 12345`

## Como usar

1. Quando o cliente se conecta, ele pode criar uma sala de chat ou participar de uma já existente
2. Para criar uma sala, o cliente envia uma mensagem com a ação "createRoom" e o nome da sala
3. Para participar de uma sala, o cliente envia uma mensagem com a ação "joinRoom" e o nome da sala
4. Para enviar uma mensagem na sala, o cliente envia uma mensagem com a ação "chat", o nome da sala e o conteúdo da mensagem

## Notas

- Este projeto é um exemplo simples de um sistema de chat. Ele não inclui funcionalidades avançadas como autenticação de usuários, persistência de mensagens, entre outras.

