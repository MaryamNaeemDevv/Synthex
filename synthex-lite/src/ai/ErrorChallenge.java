package com.synthex.ai;

public class ErrorChallenge extends Challenge {
    public ErrorChallenge(int diff, String title, String desc, String code, String raw) {
        super("ERROR", diff, title, desc, code, raw);
    }
}
