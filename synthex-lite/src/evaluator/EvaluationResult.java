package com.synthex.evaluator;

/**
 * Result of evaluating a code submission.
 *
 * Valid resultType values:
 *   CORRECT, WRONG_OUTPUT, SYNTAX_ERROR, RUNTIME_ERROR, TIMEOUT, EVALUATOR_ERROR
 */
public class EvaluationResult {
    private final String resultType;
    private final boolean correct;
    private final String message;

    public EvaluationResult(String resultType, boolean correct, String message) {
        this.resultType = resultType;
        this.correct = correct;
        this.message = message;
    }

    public String getResultType() { return resultType; }
    public boolean isCorrect()    { return correct; }
    public String getMessage()    { return message; }

    @Override
    public String toString() {
        return "[" + resultType + "] " + (correct ? "PASS" : "FAIL") + " - " + message;
    }
}
