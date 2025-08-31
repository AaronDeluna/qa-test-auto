package data;

import dto.CandidateRegistration;
import dto.EmployerRegistrationRequestDto;

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

    public static String generatePassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String generatePhone() {
        int min = 1000000;
        int max = 9999999;
        return String.format("+7988%s", new Random().nextInt(min, max));
    }

    public static CandidateRegistration validCandidate() {
        return CandidateRegistration.builder()
                .email(uniqueEmail())
                .password(generatePassword())
                .build();
    }

    public static EmployerRegistrationRequestDto validEmployer() {
        return EmployerRegistrationRequestDto.builder()
                .email(uniqueEmail())
                .password(generatePassword())
                .name(generateName())
                .phone(generatePhone())
                .build();
    }

}
