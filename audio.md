# Audio Processing

GAST has a framework for collecting and analyzing audio data. It currently supports some simple audio processing algorithms. You can add your own!


## Analyze audio data from two sources

- max amplitude from MediaRecorder using [MaxAmplitudeRecorder](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/audio/record/MaxAmplitudeRecorder.java)
- raw audio bytes from AudioRecord using [AudioClipRecorder](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/audio/record/AudioClipRecorder.java) 

## Collect and analyze audio data

- Collect using AsyncTasks like [this](https://github.com/gast-lib/gast-lib/blob/master/app/src/root/gast/playground/audio/RecordAudioTask.java) and [this](https://github.com/gast-lib/gast-lib/blob/master/app/src/root/gast/playground/audio/RecordAmplitudeTask.java)

- Analyze the data by implementing a listener. Gast has some listeners that implement various kinds of [clappers](https://github.com/gast-lib/gast-lib/tree/master/library/src/root/gast/audio/interp)

- Use GAST audio processing algorithms. There are in [this package](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/audio/processing)


