# Projet porte ouvrante !

Le projet consiste à mettre ouvrir une porte grâce à un moteur, lorsqu'un détecteur d'ultrason détecte une personne proche. De plus une lumiére s'allume lorsque la porte s'ouvre et se ferme.

De plus, un composant bluetooth est intégré au projet permettant d'être controlé par une application mobile.
On peut grâce à l'application mobile :

 - Détecter si la porte est ouverte ou fermé
 - Modifier les couleurs des leds lorsque la poste se ferme et  s'ouvre
 - Modifier la distance de détection

# Composant

Les composants principaux utilisés sont :

 - Arduino Leonardo
 - Capteur d'ultrason HC-SR04
 -  Bluetooth HC-06
 - Une led RGB
 - Servomoteur chinois "9 grammes"

## Norme code

La norme pour le code android  [![js-standard-style](https://camo.githubusercontent.com/d0f65430681b67b7104f6130ada8c098ec5f66ba/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f636f64652532307374796c652d7374616e646172642d627269676874677265656e2e7376673f7374796c653d666c6174)](https://source.android.com/setup/contribute/code-style)

La norme pour le code arduino [![js-standard-style](https://camo.githubusercontent.com/d0f65430681b67b7104f6130ada8c098ec5f66ba/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f636f64652532307374796c652d7374616e646172642d627269676874677265656e2e7376673f7374796c653d666c6174)](https://www.arduino.cc/en/Reference/StyleGuide)

## Capture d'écran

Les captures d'écrans se trouve sous dans le dossier screenshot.

## Framework / Librairie

Pour l'objet connecté :
 - Servo
 - Arduino

 Pour l'application mobile version Android Lollipop à Android Nougat :

 - Android framework
 - Butterknife

## Installation

 - Pour développer l'application mobile :

    1. Installer Android Studio 3.0
    2. Importer le projet android dans Android Studio
    3. Vous pouvez maintenant commencer à coder !

   Pour plus d'information, allez sur :
   https://developer.android.com/studio/intro/migrate.html
 - Pour développer l'objet connecté :
	 1. Installer l'[IDE Arduino](https://www.arduino.cc/en/Main/Software)
	 2. Brancher un câble USB à votre ordinateur
	 3. Modifier le fichier arduino.ino et le téléverser  

## Authors

Développeur :
Quentin LEPRAT
Mohsan BUTT

## License

GNU General Public License v3.0
