package ma.emsi.gounidimeryem.tp1gounidimeryem.jsf;

/**
 * Exception thrown when there is a problem with the LLM request.
 */
public class RequeteException extends Exception {
    public RequeteException(String message) {
        super(message);
    }
}