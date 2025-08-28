package data;

import dto.CandidateRegistration;

import java.util.Random;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static String uniqueEmail() {
        return "qa+" + System.currentTimeMillis() + "-" + UUID.randomUUID() + "@test.local";
    }

    public static String generateName() {
        return "Test name-" + UUID.randomUUID();
    }

    public static String generatePhone() {
        int min = 1000000;
        int max = 9999999;
        return String.format("+7988%s", new Random().nextInt(min, max));
    }

    public static CandidateRegistration validCandidate(String password) {
        return CandidateRegistration.builder()
                .email(uniqueEmail())
                .password(password)
                .build();
    }

}
