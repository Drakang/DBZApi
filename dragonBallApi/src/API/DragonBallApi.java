package API;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DragonBallApi {
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;

    public String token;
    public String email;
    public String password;
    public String webAddress;
    private OkHttpClient client = null;
    private Request request = null;
    public String responseBody;
    private String foto;
    private static List<Heroes> heroes = new ArrayList<>();
    public final String LOGIN_ENDPOINT = "/api/auth/login";
    public final String LIST_HEROES = "/api/heros/all";
    public final String LIST_HEROES_EVO = "/api/heros/tranformations";
    public final String URL = "https://dragonball.keepcoding.education";

    private Logs logs = new Logs();
    private boolean debugMode = false;

    public String getToken() {
        return token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    public int getHeroesCount() {
        return heroes.size();
    }

    public int getTotalEvolutionsCount() {
        int totalEvolutions = 0;
        for (Heroes hero : heroes) {
            totalEvolutions += hero.getTotalEvolutionsCount();
        }
        return totalEvolutions;
    }

    public static List<Heroes> getHeroList() {
        return heroes;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        logs.setDebugMode(debugMode);
    }

    public int getLogsCount() {
        return logs.getLogsCount();
    }

    public String getLog(int pos) {
        return logs.getLog(pos);
    }

    public int login(String email, String password) {
        webAddress = URL + LOGIN_ENDPOINT;
        JSONObject requestBody = new JSONObject();
        requestBody.put("Username", email);
        requestBody.put("Password", password);

        String credentials = email + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toString()
        );

        client = getUnsafeOkHttpClient();

        request = new Request.Builder()
                .url(webAddress)
                .addHeader("Authorization", "Basic " + base64Credentials)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            if (response.code() == 200) {
                if (debugMode) {
                    logs.addLog("Login correcto. Codigo respuesta: " + response.code());
                }
                token = response.body().string();
                setToken(token);
                return SUCCESS;
            } else {
                if (debugMode) {
                    logs.addLog("Error login. Codigo respuesta: " + response.code());
                }
                return ERROR;
            }
        } catch (Exception e) {
            if (debugMode) {
                logs.addLog("Exception Error Login. " + e.getMessage());
            }
            return ERROR;
        }
    }

    public int listHeroes(String token, String filter) {
        webAddress = URL + LIST_HEROES;
        if (filter == null) {
            filter = "";
        }
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", filter);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toString()
        );

        client = getUnsafeOkHttpClient();

        request = new Request.Builder()
                .url(webAddress)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            if (response.code() == 200) {
                if (debugMode) {
                    logs.addLog("Lista correcta. Codigo respuesta: " + response.code());
                }
                responseBody = response.body().string();
                JSONArray jsonArray = new JSONArray(responseBody);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String description = jsonObject.getString("description");
                    String photo = jsonObject.getString("photo");

                    String filename = "heroe_" + id;
                    if (photo != null && !photo.isEmpty()) {
                        foto = saveImageToFile(photo, filename);
                    }
                    Heroes hero = new Heroes(id, name, description, foto);
                    heroes.add(hero);

                    List<Heroes> evolutions = listHeroesEvo(id);
                    hero.getEvolutions().addAll(evolutions);
                }
                return SUCCESS;
            } else {
                if (debugMode) {
                    logs.addLog("Error en lista. Codigo respuesta: " + response.code());
                }
                return ERROR;
            }
        } catch (Exception e) {
            if (debugMode) {
                logs.addLog("Exception Error lista. " + e.getMessage());
            }
            return ERROR;
        }
    }

    private List<Heroes> listHeroesEvo(String ID) {
        webAddress = URL + LIST_HEROES_EVO;

        JSONObject requestBody = new JSONObject();
        requestBody.put("id", ID);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toString()
        );

        client = getUnsafeOkHttpClient();

        request = new Request.Builder()
                .url(webAddress)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            if (response.code() == 200) {
                if (debugMode) {
                    logs.addLog("Lista de Evoluciones correcta. Codigo respuesta: " + response.code());
                }
                responseBody = response.body().string();
                JSONArray jsonArray = new JSONArray(responseBody);
                List<Heroes> evolutions = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String description = jsonObject.getString("description");
                    String photo = jsonObject.getString("photo");

                    String filename = "heroe_" + id + ".jpg";
                    if (photo != null && !photo.isEmpty()) {
                        String filePath = saveImageToFile(photo, filename);
                        evolutions.add(new Heroes(id, name, description, filePath));
                    }
                }
                return evolutions;
            } else {
                if (debugMode) {
                    logs.addLog("Error en lista de evoluciones. Codigo respuesta: " + response.code());
                }
                return new ArrayList<>();
            }
        } catch (Exception e) {
            if (debugMode) {
                logs.addLog("Exception Error lista de evoluciones. " + e.getMessage());
            }
            return new ArrayList<>();
        }
    }

    private String saveImageToFile(String imageUrl, String filename) {
        OkHttpClient client = getUnsafeOkHttpClient(); // Usar el mismo cliente inseguro

        try {
            // Obtener el directorio temporal del sistema
            String tempDir = System.getProperty("java.io.tmpdir");

            // Construir la ruta completa para el archivo en el directorio temporal
            Path tempFilePath = Paths.get(tempDir, filename);

            // Eliminar archivo si ya existe
            Files.deleteIfExists(tempFilePath);

            // Crear una solicitud para la imagen
            Request request = new Request.Builder().url(imageUrl).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                try (InputStream inputStream = response.body().byteStream()) {
                    // Copiar la imagen a un archivo en el directorio temporal
                    Files.copy(inputStream, tempFilePath);
                }

                // Verificar que el archivo se guardó correctamente
                if (Files.exists(tempFilePath)) {
                    if (debugMode) {
                        logs.addLog("Éxito en el guardado de la foto. " + tempFilePath.toString());
                    }
                    return tempFilePath.toString(); // Retornar la ruta completa del archivo guardado
                } else {
                    if (debugMode) {
                        logs.addLog("Error al guardar la foto. Archivo no encontrado: " + tempFilePath.toString());
                    }
                    return null;
                }
            }
        } catch (IOException e) {
            if (debugMode) {
                logs.addLog("Exception Error en guardado de foto. " + e.getMessage());
            }
            return null;
        }
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Crear un TrustManager que confía en todos los certificados
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        // No implementar verificación de cliente
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        // No implementar verificación de servidor
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Configurar el contexto SSL con el TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Crear el OkHttpClient con el contexto SSL y un hostname verifier que confía en todos los hosts
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true); // Verificar cualquier hostname

            // Configurar tiempos de espera
            builder.connectTimeout(120, TimeUnit.SECONDS); // Tiempo de espera para la conexión
            builder.readTimeout(120, TimeUnit.SECONDS); // Tiempo de espera para la lectura
            builder.writeTimeout(120, TimeUnit.SECONDS); // Tiempo de espera para la escritura

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Métodos para obtener detalles del héroe por posición
    public String getHeroId(int position) {
        if (position >= 0 && position < heroes.size()) {
            return heroes.get(position).getId();
        } else {
            return null;
        }
    }

    public String getHeroName(int position) {
        if (position >= 0 && position < heroes.size()) {
            return heroes.get(position).getName();
        } else {
            return null;
        }
    }

    public String getHeroDescription(int position) {
        if (position >= 0 && position < heroes.size()) {
            return heroes.get(position).getDescription();
        } else {
            return null;
        }
    }

    public String getHeroPhoto(int position) {
        if (position >= 0 && position < heroes.size()) {
            return heroes.get(position).getPhoto();
        } else {
            return null;
        }
    }

    // Métodos para obtener detalles de las evoluciones del héroe por posición
    public String getEvolutionId(int heroPosition, int evoPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            if (evoPosition >= 0 && evoPosition < evolutions.size()) {
                return evolutions.get(evoPosition).getId();
            }
        }
        return null;
    }

    public String getEvolutionName(int heroPosition, int evoPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            if (evoPosition >= 0 && evoPosition < evolutions.size()) {
                return evolutions.get(evoPosition).getName();
            }
        }
        return null;
    }

    public String getEvolutionDescription(int heroPosition, int evoPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            if (evoPosition >= 0 && evoPosition < evolutions.size()) {
                return evolutions.get(evoPosition).getDescription();
            }
        }
        return null;
    }

    public String getEvolutionPhoto(int heroPosition, int evoPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            if (evoPosition >= 0 && evoPosition < evolutions.size()) {
                return evolutions.get(evoPosition).getPhoto();
            }
        }
        return null;
    }

    // Método para verificar si un héroe tiene evoluciones
    public boolean hasEvolutions(int heroPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            return !evolutions.isEmpty();
        } else {
            return false;
        }
    }

    // Método para obtener la cantidad de evoluciones de un héroe
    public int getEvolutionCount(int heroPosition) {
        if (heroPosition >= 0 && heroPosition < heroes.size()) {
            List<Heroes> evolutions = heroes.get(heroPosition).getEvolutions();
            return evolutions.size();
        } else {
            return -1; // -1 para indicar que la posición no es válida
        }
    }
}
