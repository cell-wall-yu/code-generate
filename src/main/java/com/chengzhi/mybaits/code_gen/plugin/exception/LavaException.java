package com.chengzhi.mybaits.code_gen.plugin.exception;

public class LavaException extends RuntimeException {

    /**
     * 序列化号
     */
    private static final long serialVersionUID = 883132628243816501L;


    public LavaException(String message) {
        super(message);
    }


    public LavaException(String message, Throwable cause) {
        super(message, cause);
    }
}