package tech.itpark.security;

import java.util.Arrays;
import java.util.Set;


public interface Auth {
  String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

  long getId();
  Set<String> getRoles();

  default boolean hasRole(String role) {
    return hasAnyRole(role);
  }

  default boolean hasAnyRole(String ...roles) {
    return Arrays.stream(roles)
            .anyMatch(o -> getRoles().contains(o));
  }

  default boolean isAnonymous() {
    return hasRole(ROLE_ANONYMOUS);
  }

  static Auth anonymous() {
    return new Auth() {
      @Override
      public long getId() {
        return -1;
      }

      @Override
      public Set<String> getRoles() {
        return Set.of(ROLE_ANONYMOUS);
      }
    };
  }
}
