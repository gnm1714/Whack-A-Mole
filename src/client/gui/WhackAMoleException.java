package client.gui;

/**
 * A custom exception thrown from any of the Connect Four classes if something
 * goes wrong.
 *
 * @author Gabe Megna <gnm1714@rit.edu>
 *          Nick Piwko <nap2828@rit.edu>
 */
public class WhackAMoleException extends Exception {
    /**
     * Convenience constructor to create a new {@link WhackAMoleException}
     * with an error message.
     *
     * @param message The error message associated with the exception.
     */
    public WhackAMoleException(String message) {
        super(message);
    }

    /**
     * Convenience constructor to create a new {@link WhackAMoleException}
     * as a result of some other exception.
     *
     * @param cause The root cause of the exception.
     */
    public WhackAMoleException(Throwable cause) {
        super(cause);
    }

    /**
     * * Convenience constructor to create a new {@link WhackAMoleException}
     * as a result of some other exception.
     *
     * @param message The message associated with the exception.
     * @param cause The root cause of the exception.
     */
    public WhackAMoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
