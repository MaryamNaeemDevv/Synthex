package com.synthex.evaluator;

/** Interface for code evaluation. Real impl uses Docker sandbox. */
public interface CodeEvaluator {
    EvaluationResult sendToCompiler(String userCode, String expectedOutput);
}
