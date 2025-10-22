package ma.emsi.gounidimeryem.tp1gounidimeryem.jsf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.*;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;

/**
 * Classe utilitaire pour manipuler le JSON pour l'API Gemini.
 */
@Named
@ApplicationScoped
public class JSonUtilPourGemini {
    
    @Inject
    private LlmClientPourGemini llmClient;
    
    private String roleSysteme;

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    /**
     * Envoie une requête au LLM et retourne l'interaction.
     */
    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        if (roleSysteme == null || roleSysteme.trim().isEmpty()) {
            throw new RequeteException("Le rôle système n'est pas défini");
        }

        try {
            // Format JSON pour Gemini
            // Création du JSON de requête pour Gemini selon le format officiel
            JsonObject jsonRequest = Json.createObjectBuilder()
                .add("contents", Json.createArrayBuilder()
                    // Premier message : le rôle système
                    .add(Json.createObjectBuilder()
                        .add("role", "user")
                        .add("parts", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                .add("text", roleSysteme)
                            )
                        )
                    )
                    // Deuxième message : la question de l'utilisateur
                    .add(Json.createObjectBuilder()
                        .add("role", "user")
                        .add("parts", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                .add("text", question)
                            )
                        )
                    )
                ).build();

            String requestJson = jsonRequest.toString();
            Response response = llmClient.envoyerRequete(Entity.json(requestJson));

            if (response.getStatus() != 200) {
                throw new RequeteException("Erreur API: " + response.getStatus());
            }

            String responseJson = response.readEntity(String.class);
            String reponseExtraite = extractReponseFromJson(responseJson);

            return new LlmInteraction(reponseExtraite, requestJson, responseJson);
        } catch (Exception e) {
            throw new RequeteException("Erreur lors de l'appel au LLM: " + e.getMessage());
        }
    }

    private String extractReponseFromJson(String jsonResponse) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonResponse))) {
            JsonObject root = reader.readObject();
            
            // Vérifier si nous avons des candidats
            if (!root.containsKey("candidates") || root.getJsonArray("candidates").isEmpty()) {
                throw new Exception("Pas de réponse du modèle");
            }
            
            // Obtenir le premier candidat
            JsonObject candidate = root.getJsonArray("candidates").getJsonObject(0);
            
            // Vérifier le finishReason
            if (candidate.containsKey("finishReason") && 
                !candidate.getString("finishReason").equals("STOP")) {
                throw new Exception("Génération incomplète : " + candidate.getString("finishReason"));
            }
            
            // Extraire le texte de la réponse
            return candidate.getJsonObject("content")
                          .getJsonArray("parts")
                          .getJsonObject(0)
                          .getString("text");
        } catch (Exception e) {
            return "Erreur lors du parsing de la réponse JSON: " + e.getMessage();
        }
    }
}