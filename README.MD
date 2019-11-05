# Precision Timing Audio Sequencer Module
For audio tools like drum machines, samplers, metronomes, etc.

## Overview
#### The requirements that led to this module:
 - playing clips with timing precison and accuracy that is good enough for recording a track for production
 - doing this for multiple simultaneous AudioTracks
 - able to change tempo while playing
 - able to change audio content while playing
 - able to define time position of audio clips relative to beats and bars, consistent with musical notation
 - content can be provided as looping or non-looping
 - module easily includable in an android app
 
#### Precision is achieved by:
 - streaming PCM audio data to AudioTrack instances and not allowing AudioTrack to underrun
 - keeping count of frames written to an AudioTrack
 - mapping frame-position to JVM nanotime with an AudioTimestamp
 - the time of a given item is calculated relative to the content start time and with adequate precision

## Installation
Add the pfseq AAR module to your Android project and make it a dependency for the project's app module. You can get the AAR from this demo app's **pfseq\build\outputs\aar** directory. You may need to run "rebuild project" in Android Studio to generate this.

## Usage
1. Extend PFSeq and implement the abstract method getNotification(). It will need to return a Notification object unless the RUN_IN_FOREGROUND config value is set to false.
2. Extend PFSeqActivity for any activity that will interact with your sequencer service. Abstract methods:
  - receiveMessage - The sequencer service sends error messages to the activity. Do what you want with them here.
  - onConnect - Called when the activity becomes bound. This is where you set up UI listeners that call public methods on your sequencer service (if bound).
  - getServiceClass - Just return YourSequencerService.class. It tells PFSeqActivity the name of your PFSeq base class.
3. Call setUpSequencer. Pass it a PFSeqConfig object that was instantiated with null arguments, for default config settings. Look at the PFSeqConfig constructor to see how to override default config settings
4. Add at least one track to the sequencer, and add at least one "piano roll item" with an audio clip to the track.
5. Call play() on the sequencer.

## License
Copyright (c) 2019 People's Feelings  
Licensed under GPL v2  
https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html

## Requirements
 - API level 24
