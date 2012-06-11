# Speech

GAST has some helpful utilities to help you implement speech recognition and Text-to-Speech.


## Some cool speech things you can do with GAST

- Add speech recognition and Text-to-Speech to your app quickly by extending the abstract [SpeechRecognizingAndSpeakingActivity](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/SpeechRecognizingAndSpeakingActivity.java) class.

- Run direct speech recognition using [SpeechRecognizer](http://developer.android.com/reference/android/speech/SpeechRecognizer.html)

- Run direct speech recognition continuously using SpeechRecognizer and a service. For this you need to use [WordActivator](
https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/activation/WordActivator.java) and [SpeechActivatorService](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/activation/SpeechActivationService.java)

- Use a framework to execute sequences of speech commands using [VoiceActions](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/voiceaction/VoiceAction.java) and [VoiceActionExecutor](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/voiceaction/VoiceActionExecutor.java)

- Improve recognition accuracy and recognize words Google's recognizer can't understand by using [phonetic matching](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/text/match/SoundsLikeWordMatcher.java) and [stemming](https://github.com/gast-lib/gast-lib/blob/master/library/src/root/gast/speech/text/match/StemmedWordMatcher.java) algorithms. Various algorithms are [here](https://github.com/gast-lib/gast-lib/tree/master/library/src/root/gast/speech/text)

- Search a database for matches with recognized speech using [FTS](https://github.com/gast-lib/gast-lib/blob/master/app/src/root/gast/playground/speech/food/db/FtsIndexedFoodDatabase.java) or [Lucene](https://github.com/gast-lib/gast-lib/tree/master/app/src/root/gast/playground/speech/food/lucene)