# Pentago

Pentago is a two-player board game. The game is played on a 6x6 board divided into four 3x3 quadrants. Each player is represented by a mark of colour black or white, usually the player with black mark starts. Two players take turns and place their mark on an empty field, then choose one of the quadrants to rotate it 90 degrees clockwise or counterclockwise. A player wins by getting five of their marks in a vertical, horizontal or diagonal direction after quadrant rotation in their move. In case both players win after a quadrant rotation, the game is a draw. If the board is full and no one wins, the game is a draw.

# Requirements
This project requires Java to set up and jUnit for tests.

# Usage
Below are the instructions for starting the server, and client.
### Start the server
> Go to class Server in the package networking.server and run the class.
### Start the client
> Go to class Client in the package networking.client and run the class.
>
> Enter *localhost* if you are asked to enter the address of the server.
>
> Enter port number *1234* if you are asked to enter the port number of the server.
### Start the localgame
> Go to class LocalGame in the package game and run the class.


# File Structure

* *game* - this package is the game logic part of the application. It includes the classes that are responsible for the main functionalities of the game.
    * *models* - this package contains the models part of the game.
* *networking* - this package includes the classes that are responsible for server and client communication.
    * *client* - this package contains the classes that are responsible for the client communication.
    * *dto* - this package contains data transfer objects.
    * *mappers* - this package contains classes used to map data.
    * *models* - this package contains the models part of the networking.
    * *server* -  this package contains the classes that are responsible for the sever communication.
* *test* - this package contains the test part of the game and server.
* *utils* - this package contains files that are used as utility functions provided by UT Twente
