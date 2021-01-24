package com.andrulban.sandbox.stripe.controller;

import com.andrulban.sandbox.stripe.dto.PasswordChangeRequestDto;
import com.andrulban.sandbox.stripe.dto.PasswordResetDto;
import com.andrulban.sandbox.stripe.dto.RegistrationDto;
import com.andrulban.sandbox.stripe.dto.UserDataEditionDto;
import com.andrulban.sandbox.stripe.security.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;

import static com.andrulban.sandbox.stripe.entity.User.UserRole.CUSTOMER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Requires running docker-compose because instead of in-memory DB, DB form docker-compose is used
 * to make tests run faster.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:sql/user.sql")
public class UserRestControllerTest {

  @Autowired private ObjectMapper objectMapper;
  @Autowired private WebApplicationContext context;

  private MockMvc mvc;
  private RegistrationDto registrationDto;

  @Before
  public void setUp() {
    mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    registrationDto =
        RegistrationDto.builder()
            .email("fake@gmail.com")
            .password("password")
            .firstName("f")
            .lastName("l")
            .phoneNumber("733111333")
            .build();
  }

  @Test
  public void registerUser_success() throws Exception {
    mvc.perform(
            post("/users/registration")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
        .andDo(print())
        .andExpect(status().isCreated());
  }

  // Checking if Hibernate validation works
  @Test
  public void registerUser_missingRequiredField_throwsException() throws Exception {
    RegistrationDto registrationDto = RegistrationDto.builder().email("fake@gmail.com").build();

    mvc.perform(
            post("/users/registration")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @Customer
  public void getUserById_success() throws Exception {
    MvcResult registrationResult =
        mvc.perform(
                post("/users/registration")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn();
    String locationHeader = registrationResult.getResponse().getHeader("Location");
    String createdUserId = locationHeader.split("/")[2];

    mvc.perform(get("/users/" + createdUserId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(Integer.valueOf(createdUserId))))
        .andExpect(jsonPath("$.email", is(registrationDto.getEmail())))
        .andExpect(jsonPath("$.firstName", is(registrationDto.getFirstName())))
        .andExpect(jsonPath("$.lastName", is(registrationDto.getLastName())))
        .andExpect(jsonPath("$.phoneNumber", is(registrationDto.getPhoneNumber())))
        .andExpect(jsonPath("$.userRole", is(CUSTOMER.name())));
  }

  @Test
  @Customer
  public void getUserById_nonexistentId_notFound() throws Exception {
    mvc.perform(get("/users/-1")).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  @Customer
  public void editUserData_success() throws Exception {
    UserDataEditionDto userDataEditionDto =
        UserDataEditionDto.builder()
            .email("fake@gmail.com")
            .firstName("Q")
            .lastName("W")
            .phoneNumber("111222333")
            .build();

    mvc.perform(
            put("/users")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataEditionDto)))
        .andDo(print())
        .andExpect(status().isCreated());

    // 1000000 is ID constant from the SQL script
    mvc.perform(get("/users/1000000"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1000000)))
        .andExpect(jsonPath("$.email", is(userDataEditionDto.getEmail())))
        .andExpect(jsonPath("$.firstName", is(userDataEditionDto.getFirstName())))
        .andExpect(jsonPath("$.lastName", is(userDataEditionDto.getLastName())))
        .andExpect(jsonPath("$.phoneNumber", is(userDataEditionDto.getPhoneNumber())))
        .andExpect(jsonPath("$.userRole", is(CUSTOMER.name())));
  }

  @Test
  @Customer
  public void editUserData_emailIsAlreadyUsed_throwsException() throws Exception {
    mvc.perform(
            post("/users/registration")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
        .andDo(print())
        .andExpect(status().isCreated());
    UserDataEditionDto userDataEditionDto =
        UserDataEditionDto.builder()
            .email(registrationDto.getEmail())
            .firstName("Q")
            .lastName("W")
            .phoneNumber("111222333")
            .build();

    mvc.perform(
            put("/users")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDataEditionDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.message",
                containsString("email: " + registrationDto.getEmail() + " already exists")));
  }

  @Test
  @Customer
  public void changePassword_success() throws Exception {
    PasswordChangeRequestDto passwordChangeRequestDto =
        PasswordChangeRequestDto.builder()
            .newPassword("Password123")
            // Constant taken from inserted SQL (password is hashed in SQL)
            .oldPassword("password")
            .build();

    mvc.perform(
            put("/users/password-change")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequestDto)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @Customer
  public void changePassword_wrongPassword_throwsException() throws Exception {
    PasswordChangeRequestDto passwordChangeRequestDto =
        PasswordChangeRequestDto.builder()
            .newPassword("Password123")
            .oldPassword("wrong_password")
            .build();

    mvc.perform(
            put("/users/password-change")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeRequestDto)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("Wrong password")));
  }

  @Test
  @Customer
  public void resetPassword_success() throws Exception {
    PasswordResetDto passwordResetDto =
        PasswordResetDto.builder()
            .newPassword("Password123")
            // Constant taken from inserted SQL (reset_token column)
            .token("869cc21d-f04f-4210-a1c7-1139687c3c5d")
            .build();

    mvc.perform(
            put("/users/password-reset")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetDto)))
        .andDo(print())
        .andExpect(status().isCreated());
  }
}
