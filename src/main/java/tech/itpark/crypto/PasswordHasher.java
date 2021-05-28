package tech.itpark.crypto;

public interface PasswordHasher {
  String hash(String raw, int saltLength);

  String hash(String raw);

  boolean matches(String hash, String raw);
}
