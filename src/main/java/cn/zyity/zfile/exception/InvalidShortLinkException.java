package cn.zyity.zfile.exception;

/**
 * 无效的直链异常
 */
public class InvalidShortLinkException extends RuntimeException {
    public InvalidShortLinkException() {
    }

    public InvalidShortLinkException(String message) {
        super(message);
    }

    public InvalidShortLinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidShortLinkException(Throwable cause) {
        super(cause);
    }

    public InvalidShortLinkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
