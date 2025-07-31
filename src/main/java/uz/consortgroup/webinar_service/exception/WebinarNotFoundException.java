package uz.consortgroup.webinar_service.exception;

public class WebinarNotFoundException extends RuntimeException {
    public WebinarNotFoundException(String message) {
        super(message);
    }
}
