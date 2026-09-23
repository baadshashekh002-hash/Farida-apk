package com.farida.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private TextView status;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 60, 40, 40);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Farida");
        title.setTextSize(34);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status = new TextView(this);
        status.setText("Namaste ❤️ Main Farida hoon");
        status.setTextSize(20);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        input = new EditText(this);
        input.setHint("Mujhse kuch kahiye...");
        input.setTextSize(18);
        root.addView(input);

        Button speak = new Button(this);
        speak.setText("🎤 Boliye");
        root.addView(speak);

        Button send = new Button(this);
        send.setText("💬 Bhejiye");
        root.addView(send);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
                tts.setSpeechRate(0.90f);
                tts.setPitch(1.05f);
            }
        });

        speak.setOnClickListener(v -> startListening());

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                processCommand(text);
            }
        });

        setContentView(root);

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Aapke phone mein voice recognition available nahi hai.");
            return;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                status.setText("🎤 Sun rahi hoon...");
            }

            @Override public void onBeginningOfSpeech() {}

            @Override public void onRmsChanged(float rmsdB) {}

            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                status.setText("Samajh rahi hoon...");
            }

            @Override public void onError(int error) {
                status.setText("Dobara boliye...");
            }

            @Override public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    input.setText(text);
                    processCommand(text);
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Farida ko boliye..."
        );

        speechRecognizer.startListening(intent);
    }

    private void processCommand(String text) {
        String command = text.toLowerCase(Locale.ROOT);

        if (command.contains("youtube")) {
            speak("YouTube khol rahi hoon.");
            openApp("com.google.android.youtube", "YouTube");
        } else if (command.contains("chrome") || command.contains("browser")) {
            speak("Browser khol rahi hoon.");
            openUrl("https://www.google.com");
        } else if (command.contains("whatsapp")) {
            speak("WhatsApp khol rahi hoon.");
            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage("com.whatsapp");
            if (intent != null) {
                startActivity(intent);
            } else {
                speak("WhatsApp phone mein installed nahi hai.");
            }
        } else {
            speak("Aapne kaha: " + text);
        }
    }

    private void openApp(String packageName, String appName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                startActivity(intent);
            } else {
                speak(appName + " phone mein installed nahi hai.");
            }
        } catch (Exception e) {
            speak(appName + " nahi khul pa raha hai.");
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            speak("Ye nahi khul pa raha hai.");
        }
    }

    private void speak(String text) {
        status.setText(text);
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "farida");
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
