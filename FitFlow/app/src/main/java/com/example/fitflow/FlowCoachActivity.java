package com.example.fitflow;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull; // Necesario para OkHttp callbacks
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
// Ya no se necesitan los TextInputEditText para API y Key

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException; // Necesario para OkHttp
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FlowCoachActivity extends AppCompatActivity {

    // Tus constantes de API URL y API Key (¡ASEGÚRATE QUE SEAN LAS CORRECTAS!)
    private static final String API_URL = ""; // Tu API URL de Groq
    private static final String API_KEY = ""; // Tu API Key de Groq

  
    private static final String API_MODEL = "Llama-3.1-8B-Instant";

    private MaterialToolbar toolbar;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText messageEditText;
    private ImageButton sendMessageButton;

    private Chip chipHowToStart, chipBeginnerRoutine, chipNutritionTips;

    private OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String TAG = "FlowCoachActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_coach);

        toolbar = findViewById(R.id.flowCoachToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 30 segundos es usualmente suficiente
                .readTimeout(60, TimeUnit.SECONDS)    // Tiempo de espera para leer la respuesta (puede ser más largo para IA)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        chipHowToStart = findViewById(R.id.chipHowToStart);
        chipBeginnerRoutine = findViewById(R.id.chipBeginnerRoutine);
        chipNutritionTips = findViewById(R.id.chipNutritionTips);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        addWelcomeMessage();

        sendMessageButton.setOnClickListener(v -> handleSendMessage());

        chipHowToStart.setOnClickListener(v -> sendSuggestionAsMessage(chipHowToStart.getText().toString()));
        chipBeginnerRoutine.setOnClickListener(v -> sendSuggestionAsMessage(chipBeginnerRoutine.getText().toString()));
        chipNutritionTips.setOnClickListener(v -> sendSuggestionAsMessage(chipNutritionTips.getText().toString()));
    }

    
private void addWelcomeMessage() {
        ChatMessage welcomeMsg = new ChatMessage(
                getString(R.string.flowcoach_welcome_message),
                getCurrentTime(),
                false,
                getString(R.string.flowcoach_title)
        );
        messageList.add(welcomeMsg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
    }

    private void handleSendMessage() {
        String text = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        sendMessage(text); // Muestra el mensaje del usuario inmediatamente
        messageEditText.setText("");
        getApiResponse(text);
    }

    private void sendSuggestionAsMessage(String suggestionText) {
        sendMessage(suggestionText); // Muestra la sugerencia como mensaje del usuario
        getApiResponse(suggestionText);
    }

    private void sendMessage(String text) {
        ChatMessage userMessage = new ChatMessage(text, getCurrentTime(), true, "Tú");
        messageList.add(userMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String text) {
        ChatMessage botMessage = new ChatMessage(text, getCurrentTime(), false, getString(R.string.flowcoach_title));
        messageList.add(botMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void getApiResponse(String userMessage) {
        if (TextUtils.isEmpty(API_URL) || API_URL.equals("TU_API_URL_AQUI") || // Verificación por si acaso
            TextUtils.isEmpty(API_KEY) || API_KEY.equals("TU_API_KEY_AQUI")) { // Verificación por si acaso
            addBotMessage("La configuración de la API (URL o Key) no está definida correctamente en el código. Por favor, contacta al desarrollador.");
            Log.e(TAG, "API_URL o API_KEY no configuradas con valores reales en el código.");
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            // Cuerpo de la solicitud compatible con OpenAI
            jsonBody.put("model", API_MODEL); // ¡¡Importante: Especificar el modelo!!

            JSONArray messagesArray = new JSONArray();
            JSONObject userMessageJson = new JSONObject();
            userMessageJson.put("role", "user");
            userMessageJson.put("content", userMessage);
            messagesArray.put(userMessageJson);

            jsonBody.put("messages", messagesArray);

            // Opcional: puedes añadir otros parámetros si la API los soporta y los necesitas
            // jsonBody.put("temperature", 0.7);
            // jsonBody.put("max_tokens", 150);

        } catch (JSONException e) {
            Log.e(TAG, "Error creando el cuerpo JSON para la API", e);
            addBotMessage("Error interno al preparar tu mensaje.");
            return;
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        Log.d(TAG, "Enviando solicitud a API: " + API_URL);
        Log.d(TAG, "Usando modelo: " + API_MODEL);
        Log.d(TAG, "Cuerpo JSON: " + jsonBody.toString());

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Fallo en la llamada a la API: ", e);
                runOnUiThread(() -> {
                    addBotMessage("No se pudo conectar con el asistente FlowCoach. Revisa tu conexión a internet.");
                    Toast.makeText(FlowCoachActivity.this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseString;
                try (ResponseBody responseBody = response.body()) {
                    responseString = responseBody != null ? responseBody.string() : "";
                } // try-with-resources cierra el responseBody automáticamente

                Log.d(TAG, "Respuesta de la API (código " + response.code() + "): " + responseString);

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        String errorDetails = responseString.isEmpty() ? "(sin detalles)" : responseString.substring(0, Math.min(responseString.length(), 200));
                        addBotMessage("FlowCoach no pudo procesar tu solicitud (Error " + response.code() + "). Detalles: " + errorDetails);
                        Toast.makeText(FlowCoachActivity.this, "Error de API: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                if (responseString.isEmpty()){
                    runOnUiThread(() -> addBotMessage("FlowCoach no ha respondido esta vez (respuesta vacía). Inténtalo de nuevo."));
                    return;
                }

                String botReply = parseOpenAIChatCompletionResponse(responseString);
                
                final String finalBotReply = botReply;
                runOnUiThread(() -> {
                    if (TextUtils.isEmpty(finalBotReply)){
                         addBotMessage("No se pudo entender la respuesta de FlowCoach. Intenta reformular tu pregunta.");
                    } else {
                        addBotMessage(finalBotReply);
                    }
                });
            }
        });
    }

    private String parseOpenAIChatCompletionResponse(String jsonResponseString) {
        try {
            JSONObject jsonResponse = new JSONObject(jsonResponseString);
            if (jsonResponse.has("choices")) {
                JSONArray choicesArray = jsonResponse.getJSONArray("choices");
                if (choicesArray.length() > 0) {
                    JSONObject firstChoice = choicesArray.getJSONObject(0);
                    if (firstChoice.has("message")) {
                        JSONObject messageObject = firstChoice.getJSONObject("message");
                        if (messageObject.has("content")) {
                            return messageObject.getString("content").trim();
                        }
                    }
                    // Fallback por si la estructura es ligeramente diferente (ej. algunos modelos devuelven "text" dentro de "choices")
                    if (firstChoice.has("text")) { 
                        return firstChoice.getString("text").trim();
                    }
                }
            }
            // Fallback para otros formatos comunes si la estructura de "choices" no coincide o falta
            if (jsonResponse.has("generated_text")) { 
                return jsonResponse.getString("generated_text").trim();
            }
            if (jsonResponse.has("text")) { 
                return jsonResponse.getString("text").trim();
            }
            // Si después de todos los intentos no se encuentra, loguear y devolver un mensaje de error o la respuesta cruda.
            Log.w(TAG, "No se encontró 'content', 'text' o 'generated_text' en la respuesta JSON estructurada: " + jsonResponseString.substring(0, Math.min(jsonResponseString.length(), 300)) + "...");
            return "Respuesta recibida, pero no se pudo extraer el mensaje principal.";
        } catch (JSONException e) {
            Log.e(TAG, "Error parseando JSON de la API: " + jsonResponseString, e);
            return "No se pudo interpretar la respuesta de FlowCoach (formato inesperado).";
        }
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); 
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
