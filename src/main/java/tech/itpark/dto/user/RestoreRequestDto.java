package tech.itpark.dto.user;


import lombok.Value;

@Value
public class RestoreRequestDto {
  String login;
  String password;
  String secret;
}
