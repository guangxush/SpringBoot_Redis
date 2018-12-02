package com.shgx.redis.exception;

public class CacheException extends RuntimeException {

    public CacheException(String message){ super(message);}

    public CacheException(String message, Throwable cause){ super(message, cause);}
}
