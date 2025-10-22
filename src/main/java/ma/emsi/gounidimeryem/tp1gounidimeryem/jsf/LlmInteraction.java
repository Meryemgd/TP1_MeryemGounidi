package ma.emsi.gounidimeryem.tp1gounidimeryem.jsf;

/**
 * Record pour stocker la réponse du LLM, le JSON de la requête et le JSON de la réponse.
 */
public record LlmInteraction(String reponseExtraite, String questionJson, String reponseJson) {
}