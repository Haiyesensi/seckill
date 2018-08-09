package org.seckill.exception;


/**
 * 重复秒杀异常(runtime exception)
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatKillException(String message) {
        super(message);
    }
}
