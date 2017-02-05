# SportsPersonGame

This is a guessing game for Android.

You can pull in your Android Studio or just drag and drop the .apk file into your virtual device.

Game intructions:

• Choose one of the two players

• If the player you chose has the most points you win

• The more rounds you win, the higher your score

How I made it:

I parse the JSON with all of the players and save it into a Realm Database in order to be able to play when the user doesn't have network connection.
First I get two random numbers with range the size of the people list. I check they are not the same and then I print them on the screen. To print the images I used Picasso library that is for downloading and caching them. It is very powerfull and easy to use.

When the user choose one player I disable the buttons and show a message saying if the user won or lost with a button to play a new round. The user can also see the points of each player to check it and the score will increase.

If the user click on play again button the layout will restart and they can play again.

In the menu bar you can find an info icon to see the instructions and a dropdown menu to exit.