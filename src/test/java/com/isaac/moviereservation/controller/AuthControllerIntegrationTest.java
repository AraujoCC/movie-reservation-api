package com.isaac.moviereservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaac.moviereservation.IntegrationTestBase;
import com.isaac.moviereservation.dto.auth.LoginRequest;
import com.isaac.moviereservation.dto.auth.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController — integração")
class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("deve registrar usuário e retornar 201 com token JWT")
        void shouldRegisterAndReturn201() throws Exception {
            var request = new RegisterRequest("Isaac Pietro", "isaac@test.com", "senha123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("isaac@test.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"));
        }

        @Test
        @DisplayName("deve retornar 409 quando email já está em uso")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            var request = new RegisterRequest("Isaac", "duplicate@test.com", "senha123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("deve retornar 400 quando name está em branco")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            var request = new RegisterRequest("", "valid@test.com", "senha123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando email é inválido")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            var request = new RegisterRequest("Isaac", "nao-e-email", "senha123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando senha tem menos de 8 caracteres")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            var request = new RegisterRequest("Isaac", "valid@test.com", "abc");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("deve logar e retornar 200 com token JWT")
        void shouldLoginAndReturn200() throws Exception {
            var register = new RegisterRequest("Isaac Login", "login@test.com", "senha123");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(register)))
                    .andExpect(status().isCreated());

            var login = new LoginRequest("login@test.com", "senha123");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("login@test.com"));
        }

        @Test
        @DisplayName("deve retornar 401 quando senha está errada")
        void shouldReturn401WhenPasswordIsWrong() throws Exception {
            var register = new RegisterRequest("Isaac", "wrong@test.com", "senha123");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(register)))
                    .andExpect(status().isCreated());

            var login = new LoginRequest("wrong@test.com", "senhaerrada");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deve retornar 401 quando usuário não existe")
        void shouldReturn401WhenUserDoesNotExist() throws Exception {
            var login = new LoginRequest("naoexiste@test.com", "senha123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }
    }
}