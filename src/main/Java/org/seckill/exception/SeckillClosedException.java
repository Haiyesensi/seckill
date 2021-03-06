package org.seckill.exception;


/**
 * 秒杀已经关闭(runtime)
 */
public class SeckillClosedException extends SeckillException {

    public SeckillClosedException(String message) {
        super(message);
    }

    public SeckillClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
