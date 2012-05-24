 
================================================================================
Professional Android Sensor Programming
By Greg Milette and Adam Stroud
Source Code
================================================================================

The files here contain the code from the book. 
All the code is licensed under the Apache 2.0 license. See LICENSE.txt for
license details.

================================================================================
Great Android Sensing Toolkit (GAST)
================================================================================

The code in this book is also part of an open source project called Great
Android Sensing Toolkit (GAST). See the Github site for the latest
updates: https://github.com/gast-lib.

The companion app to the book is also available on Google Play and can be
downloaded from:
https://play.google.com/store/apps/details?id=root.gast.playground.

================================================================================
  Software Requirements
================================================================================

The code was developed using the Eclipse IDE which is freely available from
http://www.eclipse.org. In addition to eclipse, the Android SDK and Android
developer's toolkit (ADT) will also need to be installed. See
http://developer.android.com/sdk/index.html for instructions on installing the
Android SDK and the ADT.

================================================================================
  Setup
================================================================================

Once the afore mentioned software packages have been installed, the source
tree can be imported into Eclipse. The provided directories contain the
necessary Eclipse project files. The projects associated with the code can
be imported into Eclipse by selecting "Import..." from the Eclipse "File" menu.
From the "Import" dialog, select "Existing Projects into Workspace" under the
"General" folder and click "Next". From the next screen, click the "Browse"
button and select the unzipped folder as the root directory. Once the unzipped
folder has been selected, the "Projects" portion of the dialog should list the
following projects that are available for import:

* GastApp               (/app)
* GastAppTest           (/appTest)
* GastLibraryLuceneExt  (/libraryLuceneExt)
* GastLibrary           (/library)
* jjil                  (/jjil)
* JJIL-Android          (/JJIL-Android)
* TemperatureSensor     (/openaccessory)

It is recommended to import all of the projects as this should create a
workspace that has the ability to run the examples with very little
"tweaking." 

Note: The directory names located on the filesystem DO NOT match the names of
the projects. The filesystem directory names are located in the above list in
parentheses where root ("/") is the directory that was created by expanding the
.zip file.

================================================================================
  Projects
================================================================================

The following is a summary of the eclipse projects that can be imported from the
previous section, as well as which chapters in the book reference the code:

* GastApp - Contains the book's demonstration app called Android Sensing
  Playground. It produces the APK that can be installed and run on an Android
  device. It has code from all chapters.

* GastAppTest - Contains unit tests for GastApp and GastLibrary. 
  It has code from chapter 17.

* GastLibraryLuceneExt - Contains a Lucene related code for speech recognition. 
  It has code from chapter 17.

* GastLibrary - Contains reusable code that supports the code from GastApp. 
  It has minimal dependencies so you can include only this project in your app.
  It has code for various chapters in the book.

* jjil - A clone of Jon's Java Imaging Library (http://code.google.com/p/jjil/) 
  with some updates for this book. It has code for chapters 12 and 13.

* JJIL-Android - Another project from http://code.google.com/p/jjil/ with some 
  updates for this book. It has code for chapters 12 and 13.

* openaccessory - Contains the code for Android Open Accessory (AOA). 
  It has code for chapter 10.

================================================================================
 Package Structure
================================================================================

The packages in GastApp are organized by sensor type and two categories:

 * root.gast.playground.<sensor type> are from GastApp
 * root.gast.<sensor type> are packages from other projects.

When you are looking for code for a particular chapter or sensor look for the
package with the appropriate sensor type.
Part 1: root.gast.playground.location
Part 2: root.gast.playground.sensor
Chapter 10: com.example.temperaturesensor (in the openaccessory folder)
Chapter 11: root.gast.playground.nfc
Chapter 12,13: root.gast.playground.image
Chapter 14: root.gast.playground.audio
Part 4: root.gast.playground.speech

================================================================================
 Contact
================================================================================

We hope this code helps you understand how to use sensors and helps you get
started using them. 

To contact the authors:
Email: gastlib@gmail.com
Github: https://github.com/gast-lib
Wrox Programmer Forums: http://p2p.wrox.com/

