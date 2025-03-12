package json;

public class JsonException extends Exception {
    private static final long serialVersionUID = 1L;

    private int location;

    public int getLocation() {
	return location;
    }

    public void setLocation(int location) {
	this.location = location;
    }

    public JsonException() {
	super();
    }

    public JsonException(String message) {
	super(message);
    }

    public JsonException(String message, Throwable cause) {
	super(message, cause);
    }

    public JsonException(Throwable cause) {
	super(cause);
    }

    public JsonException(int location) {
	super();
	this.location = location;
    }

    public JsonException(int location, String message) {
	super(message);
	this.location = location;
    }

    public JsonException(int location, String message, Throwable cause) {
	super(message, cause);
	this.location = location;
    }
}
