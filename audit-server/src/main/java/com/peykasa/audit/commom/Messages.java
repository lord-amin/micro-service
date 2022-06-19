package com.peykasa.audit.commom;

/**
 * @author Yaser(amin) Sadeghi
 */
public abstract class Messages {
    private Messages() {
    }

    public static final String START_NULL = "start date is null";
    public static final String END_NULL = "end date is null";
    public static final String START_BIGGER = "start date is bigger than end date";
    public static final String MAX_UI = "Max ui result is %s, request result is %s";
    public static final String INPUT_NULL = "input data is null";
    public static final String ACTOR_NULL = "actor is null or empty";
    public static final String CONTEXT_NULL = "context is null or empty";
    public static final String EVENT_NULL = "event is null or empty";
    public static final String TIME_NULL = "time is null or empty";

}
