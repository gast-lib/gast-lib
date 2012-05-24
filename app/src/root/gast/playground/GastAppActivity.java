/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package root.gast.playground;

import root.gast.R;
import root.gast.playground.audio.ClapperPlay;
import root.gast.playground.image.detectfaces.DetectFacesActivity;
import root.gast.playground.image.livecapture.LiveCaptureActivity;
import root.gast.playground.image.livecaptureplus.LiveCapturePlusActivity;
import root.gast.playground.image.simplecapture.SimpleCaptureActivity;
import root.gast.playground.location.CurrentLocationActivity;
import root.gast.playground.location.ProximityAlertActivity;
import root.gast.playground.location.TrackLocationActivity;
import root.gast.playground.nfc.BeamInventoryActivity;
import root.gast.playground.nfc.NFCInventoryActivity;
import root.gast.playground.nfc.Peer2PeerNFCInventoryActivity;
import root.gast.playground.sensor.NorthFinder;
import root.gast.playground.sensor.SensorListActivity;
import root.gast.playground.sensor.altitude.DetermineAltitudeActivity;
import root.gast.playground.sensor.movement.DetermineMovementActivity;
import root.gast.playground.sensor.orientation.DetermineOrientationActivity;
import root.gast.playground.speech.SayMagicWordActivity;
import root.gast.playground.speech.SayMagicWordDemo;
import root.gast.playground.speech.SayMagicWordExecutorDemo;
import root.gast.playground.speech.SpeechActivationServicePlay;
import root.gast.playground.speech.SpeechActivatorStartStop;
import root.gast.playground.speech.SpeechRecognitionPlay;
import root.gast.playground.speech.food.FoodDialogPlay;
import root.gast.playground.speech.food.MultiTurnFoodDialogActivity;
import root.gast.playground.speech.tts.TextToSpeechDemo;
import root.gast.playground.speech.tts.TextToSpeechInfoActivity;
import root.gast.playground.speech.tts.TextToSpeechPlay;
import root.gast.playground.util.DialogGenerator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

/**
 * The main activity for the app.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class GastAppActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onLaunchGetCurrentLocationActivityClick(View view) 
    {
        startActivity(new Intent(this, CurrentLocationActivity.class));
    }

    public void onLaunchTrackLocationActivityClick(View view) 
    {
        startActivity(new Intent(this, TrackLocationActivity.class));
    }

    public void onLaunchProximityAlertActivityClick(View view) 
    {
        startActivity(new Intent(this, ProximityAlertActivity.class));
    }

    public void onLaunchSensorListClick(View view) 
    {
        startActivity(new Intent(this, SensorListActivity.class));
    }
    
    public void onLaunchNorthFinderClick(View view) 
    {
        startActivity(new Intent(this, NorthFinder.class));
    }
    
    public void onLaunchDetermineOrientationClick(View view) 
    {
        startActivity(new Intent(this, DetermineOrientationActivity.class));
    }
    
    public void onLaunchDetermineMovementClick(View view) 
    {
        startActivity(new Intent(this, DetermineMovementActivity.class));
    }

    public void onLaunchSpeechPlay(View view)
    {
        startActivity(new Intent(this, SpeechRecognitionPlay.class));
    }

    public void onLaunchMagicWord(View view)
    {
        startActivity(new Intent(this, SayMagicWordActivity.class));
    }
    
    public void onLaunchMagicWordDemo(View view)
    {
        startActivity(new Intent(this, SayMagicWordDemo.class));
    }
    
    public void onlaunchMagicWordExecutor(View view)
    {
        startActivity(new Intent(this, SayMagicWordExecutorDemo.class));
    }
    
    public void onLaunchFoodDialog(View view)
    {
        startActivity(new Intent(this, FoodDialogPlay.class));
    }
    
    public void onLaunchMultiTurnFoodDialog(View view)
    {
        startActivity(new Intent(this, MultiTurnFoodDialogActivity.class));
    } 

    public void onLaunchSpeechActivationStartStop(View view)
    {
        startActivity(new Intent(this, SpeechActivatorStartStop.class));
    } 

    public void onLaunchSpeechActivationService(View view)
    {
        startActivity(new Intent(this, SpeechActivationServicePlay.class));
    } 

    public void onLaunchTextToSpeechDemo(View view)
    {
        startActivity(new Intent(this, TextToSpeechDemo.class));
    }

    public void onLaunchTextToSpeechPlay(View view)
    {
        startActivity(new Intent(this, TextToSpeechPlay.class));
    }
    
    public void onLaunchTextToSpeechInfo(View view)
    {
        startActivity(new Intent(this, TextToSpeechInfoActivity.class));
    }

    public void onLaunchNfc(View view)
    {
        startActivity(new Intent(this, NFCInventoryActivity.class));
    }
    
    public void onLaunchNfcPeer(View view)
    {
        if (Build.VERSION.SDK_INT < 10)
        {
            DialogGenerator.createInfoDialog(this, getString(R.string.d_info),
                    "sdk 10 or above is required").show();
        }
        else
        {
            startActivity(new Intent(this, Peer2PeerNFCInventoryActivity.class));
        }
    }

    public void onLaunchNfcBeam(View view)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            DialogGenerator.createInfoDialog(this, getString(R.string.d_info),
                    "Android 4.0 and above is required").show();
        }
        else
        {
            startActivity(new Intent(this, BeamInventoryActivity.class));
        }
    }

    public void onLaunchLiveCapture(View view)
    {
        startActivity(new Intent(this, LiveCaptureActivity.class));
    }

    public void onLaunchLiveCapturePlus(View view)
    {
        startActivity(new Intent(this, LiveCapturePlusActivity.class));
    }

    public void onLaunchSimpleCaptureActivity(View view)
    {
        startActivity(new Intent(this, SimpleCaptureActivity.class));
    }
    
    public void onLaunchImageBarcode(View view)
    {
        startActivity(new Intent(this, jjil.app.barcodereader.BarcodeReaderActivity.class));
    }

    public void onLaunchLaunchImageDetectLogo(View view)
    {
        startActivity(new Intent(this, root.gast.playground.image.detectlogo.DetectLogoActivity.class));
    }

    public void onLaunchLaunchImageDetectLogoBetter(View view)
    {
        startActivity(new Intent(this, root.gast.playground.image.detectlogobetter.DetectLogoActivity.class));
    }

    public void onLaunchLaunchImageDetectLogoFaster(View view)
    {
        startActivity(new Intent(this, root.gast.playground.image.detectlogofaster.DetectLogoActivity.class));
    }
    
    public void onLaunchDetectFaces(View view)
    {
        startActivity(new Intent(this, DetectFacesActivity.class));
    }
    
    public void onLaunchLaunchNfc(View view)
    {
        startActivity(new Intent(this, NFCInventoryActivity.class));
    }

    public void onLaunchClapper(View view)
    {
        startActivity(new Intent(this, ClapperPlay.class));
    }
    
    public void onLaunchDetermineAltitudeClick(View view)
    {
        startActivity(new Intent(this, DetermineAltitudeActivity.class));
    }
}